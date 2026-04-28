import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export interface BatchJobExecution {
  id: number;
  jobName: string;
  status: string;
  exitCode: string;
  exitMessage: string;
  startTime: string;
  endTime: string;
  createTime: string;
}

@Component({
  selector: 'app-batch-job-status',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './batch-job-status.component.html',
  styleUrl: './batch-job-status.component.css'
})
export class BatchJobStatusComponent implements OnInit {
  jobExecutions = signal<BatchJobExecution[]>([]);
  loading = signal<boolean>(false);
  error = signal<string>('');

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadJobs();
  }

  loadJobs(): void {
    this.loading.set(true);
    this.error.set('');
    this.http.get<BatchJobExecution[]>(`${environment.apiUrl}/admin/batch/jobs`).subscribe({
      next: (data) => {
        this.jobExecutions.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set('Failed to load batch jobs.');
        this.loading.set(false);
        console.error(err);
      }
    });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'COMPLETED': return 'status-success';
      case 'FAILED': return 'status-danger';
      case 'STARTED':
      case 'STARTING': return 'status-warning';
      default: return 'status-info';
    }
  }

  getDuration(job: BatchJobExecution): string {
    if (job.startTime && job.endTime) {
      const start = new Date(job.startTime).getTime(); // Convert string to Date object, then get milliseconds
      const end = new Date(job.endTime).getTime();
      const durationSeconds = (end - start) / 1000;
      return `${durationSeconds.toFixed(0)}s`;
    }
    return '';
  }
}
