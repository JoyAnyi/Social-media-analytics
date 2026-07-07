package com.company.socialanalytics.processing;

import com.company.socialanalytics.feed.RawPostEvent;
import com.company.socialanalytics.feed.TextExtractionService;
import com.company.socialanalytics.post.Hashtag;
import com.company.socialanalytics.post.HashtagRepository;
import com.company.socialanalytics.post.Keyword;
import com.company.socialanalytics.post.KeywordRepository;
import com.company.socialanalytics.post.SentimentAnalysis;
import com.company.socialanalytics.post.SocialPost;
import com.company.socialanalytics.post.SocialPostRepository;
import com.company.socialanalytics.realtime.RealtimeChannel;
import com.company.socialanalytics.realtime.RealtimeEventPublisher;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class PostProcessingService {
    private final SocialPostRepository socialPostRepository;
    private final HashtagRepository hashtagRepository;
    private final KeywordRepository keywordRepository;
    private final TextExtractionService textExtractionService;
    private final SentimentAnalyzer sentimentAnalyzer;
    private final ProcessedPostEventPublisher processedPostEventPublisher;
    private final RealtimeEventPublisher realtimeEventPublisher;
    private final Validator validator;

    public PostProcessingService(
            SocialPostRepository socialPostRepository,
            HashtagRepository hashtagRepository,
            KeywordRepository keywordRepository,
            TextExtractionService textExtractionService,
            SentimentAnalyzer sentimentAnalyzer,
            ProcessedPostEventPublisher processedPostEventPublisher,
            RealtimeEventPublisher realtimeEventPublisher,
            Validator validator
    ) {
        this.socialPostRepository = socialPostRepository;
        this.hashtagRepository = hashtagRepository;
        this.keywordRepository = keywordRepository;
        this.textExtractionService = textExtractionService;
        this.sentimentAnalyzer = sentimentAnalyzer;
        this.processedPostEventPublisher = processedPostEventPublisher;
        this.realtimeEventPublisher = realtimeEventPublisher;
        this.validator = validator;
    }

    @Transactional
    public ProcessedPostResponse process(RawPostEvent event) {
        validate(event);
        ProcessedPostResponse response = socialPostRepository.findWithDetailsByExternalId(event.externalId())
                .map(this::toResponse)
                .orElseGet(() -> createPost(event));
        processedPostEventPublisher.publish(toEvent(response));
        publishAfterCommit(response);
        return response;
    }

    private ProcessedPostResponse createPost(RawPostEvent event) {
        SocialPost post = new SocialPost(
                event.externalId(),
                event.platform(),
                event.authorUsername(),
                event.authorDisplayName(),
                event.content(),
                event.language(),
                event.publishedAt()
        );

        resolveHashtags(event)
                .forEach(tag -> post.addHashtag(findOrCreateHashtag(tag)));
        resolveMentions(event)
                .forEach(post::addMention);
        textExtractionService.extractKeywords(event.content())
                .forEach(term -> post.addKeyword(findOrCreateKeyword(term)));

        SentimentResult sentiment = sentimentAnalyzer.analyze(event.content());
        post.setSentimentAnalysis(new SentimentAnalysis(
                post,
                sentiment.label(),
                sentiment.score(),
                sentiment.positiveHits(),
                sentiment.negativeHits()
        ));

        try {
            SocialPost saved = socialPostRepository.saveAndFlush(post);
            return toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            return socialPostRepository.findWithDetailsByExternalId(event.externalId())
                    .map(this::toResponse)
                    .orElseThrow(() -> ex);
        }
    }

    private Hashtag findOrCreateHashtag(String tag) {
        return hashtagRepository.findByTag(tag).orElseGet(() -> saveHashtag(tag));
    }

    private Keyword findOrCreateKeyword(String term) {
        return keywordRepository.findByTerm(term).orElseGet(() -> saveKeyword(term));
    }

    private ProcessedPostResponse toResponse(SocialPost post) {
        Set<String> hashtags = post.getHashtags().stream()
                .map(Hashtag::getTag)
                .collect(Collectors.toUnmodifiableSet());
        Set<String> mentions = post.getMentions().stream()
                .map(mention -> mention.getUsername())
                .collect(Collectors.toUnmodifiableSet());
        Set<String> keywords = post.getKeywords().stream()
                .map(Keyword::getTerm)
                .collect(Collectors.toUnmodifiableSet());
        SentimentAnalysis sentiment = post.getSentimentAnalysis();
        return new ProcessedPostResponse(
                post.getExternalId(),
                post.getPlatform(),
                sentiment.getLabel(),
                sentiment.getScore(),
                hashtags,
                mentions,
                keywords
        );
    }

    private Hashtag saveHashtag(String tag) {
        try {
            return hashtagRepository.saveAndFlush(new Hashtag(tag));
        } catch (DataIntegrityViolationException ex) {
            return hashtagRepository.findByTag(tag).orElseThrow(() -> ex);
        }
    }

    private Keyword saveKeyword(String term) {
        try {
            return keywordRepository.saveAndFlush(new Keyword(term));
        } catch (DataIntegrityViolationException ex) {
            return keywordRepository.findByTerm(term).orElseThrow(() -> ex);
        }
    }

    private Set<String> resolveHashtags(RawPostEvent event) {
        return event.hashtags().isEmpty() ? textExtractionService.extractHashtags(event.content()) : event.hashtags();
    }

    private Set<String> resolveMentions(RawPostEvent event) {
        return event.mentions().isEmpty() ? textExtractionService.extractMentions(event.content()) : event.mentions();
    }

    private ProcessedPostEvent toEvent(ProcessedPostResponse response) {
        return new ProcessedPostEvent(
                response.externalId(),
                response.platform(),
                response.sentiment(),
                response.sentimentScore(),
                response.hashtags(),
                response.mentions(),
                response.keywords()
        );
    }

    private void validate(RawPostEvent event) {
        Set<ConstraintViolation<RawPostEvent>> violations = validator.validate(event);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    private void publishAfterCommit(ProcessedPostResponse response) {
        Runnable publish = () -> {
            realtimeEventPublisher.publish(RealtimeChannel.NEW_POST, response);
            realtimeEventPublisher.publish(RealtimeChannel.ANALYTICS_UPDATE, response);
            realtimeEventPublisher.publish(RealtimeChannel.DASHBOARD_UPDATES, response);
        };
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            publish.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                publish.run();
            }
        });
    }
}
