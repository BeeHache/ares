import { Component, EventEmitter, Output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FeedService } from '../feed.service';

@Component({
  selector: 'app-opml-import',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './opml-import.component.html',
  styleUrl: './opml-import.component.css'
})
export class OpmlImportComponent {
  @Output() close = new EventEmitter<void>();
  @Output() importSuccess = new EventEmitter<void>();

  // Signals
  mode = signal<'file' | 'url'>('file');
  url = signal<string>('');
  selectedFile = signal<File | null>(null);
  isLoading = signal<boolean>(false);
  errorMessage = signal<string>('');

  constructor(private feedService: FeedService) {}

  onFileSelected(event: any) {
    this.selectedFile.set(event.target.files[0]);
  }

  onSubmit() {
    this.isLoading.set(true);
    this.errorMessage.set('');

    if (this.mode() === 'file') {
      const file = this.selectedFile();
      if (!file) {
        this.errorMessage.set('Please select a file.');
        this.isLoading.set(false);
        return;
      }
      this.feedService.importOpmlFile(file).subscribe({
        next: () => this.handleSuccess(),
        error: (err) => this.handleError(err)
      });
    } else {
      const urlVal = this.url();
      if (!urlVal) {
        this.errorMessage.set('Please enter a URL.');
        this.isLoading.set(false);
        return;
      }
      this.feedService.importOpmlUrl(urlVal).subscribe({
        next: () => this.handleSuccess(),
        error: (err) => this.handleError(err)
      });
    }
  }

  private handleSuccess() {
    this.isLoading.set(false);
    this.importSuccess.emit();
    this.close.emit();
  }

  private handleError(err: any) {
    this.isLoading.set(false);
    this.errorMessage.set(err.error?.message || 'Import failed. Please try again.');
    console.error('OPML Import Error:', err);
  }
}
