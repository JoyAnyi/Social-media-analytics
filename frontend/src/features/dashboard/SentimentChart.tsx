import { useEffect, useRef } from 'react';
import { ArcElement, Chart, DoughnutController, Legend, Tooltip } from 'chart.js';
import type { SentimentBreakdown } from '../../api/client';

Chart.register(ArcElement, DoughnutController, Tooltip, Legend);

interface SentimentChartProps {
  sentiment: SentimentBreakdown;
}

export function SentimentChart({ sentiment }: SentimentChartProps) {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const chartRef = useRef<Chart<'doughnut', number[], string> | null>(null);

  useEffect(() => {
    if (!canvasRef.current) {
      return undefined;
    }
    chartRef.current = new Chart(canvasRef.current, {
      type: 'doughnut',
      data: {
        labels: ['Positive', 'Neutral', 'Negative'],
        datasets: [
          {
            data: [sentiment.positive, sentiment.neutral, sentiment.negative],
            backgroundColor: ['#0f9f9a', '#d99920', '#ef6f6c'],
            borderWidth: 0,
          },
        ],
      },
      options: {
        responsive: true,
        plugins: {
          legend: {
            position: 'bottom',
            labels: {
              boxWidth: 10,
              usePointStyle: true,
            },
          },
        },
      },
    });

    return () => chartRef.current?.destroy();
  }, []);

  useEffect(() => {
    if (!chartRef.current) {
      return;
    }
    chartRef.current.data.datasets[0].data = [sentiment.positive, sentiment.neutral, sentiment.negative];
    chartRef.current.update();
  }, [sentiment]);

  return <canvas aria-label="Sentiment distribution" ref={canvasRef} role="img" />;
}
