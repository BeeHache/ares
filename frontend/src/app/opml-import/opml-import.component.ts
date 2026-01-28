import { Component, EventEmitter, Output } from '@angular/core';
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

  mode: 'file' | 'url' = 'file';
  url = '';
  selectedFile: File | null = null;
  isLoading = false;
  errorMessage = '';

  constructor(private feedService: FeedService) {}

  onFileSelected(event: any) {
    this.selectedFile = event.target.files[0];
  }

  onSubmit() {
    this.isLoading = true;
    this.errorMessage = '';

    if (this.mode === 'file') {
      if (!this.selectedFile) {
        this.errorMessage = 'Please select a file.';
        this.isLoading = false;
        return;
      }
      this.feedService.importOpmlFile(this.selectedFile).subscribe({
        next: () => this.handleSuccess(),
        error: (err) => this.handleError(err)
      });
    } else {
      if (!this.url) {
        this.errorMessage = 'Please enter a URL.';
        this.isLoading = false;
        return;
      }
      this.feedService.importOpmlUrl(this.url).subscribe({
        next: () => this.handleSuccess(),
        error: (err) => this.handleError(err)
      });
    }
  }

  private handleSuccess() {
    this.isLoading = false;
    this.importSuccess.emit();
    this.close.emit();
  }

  private handleError(err: any) {
    this.isLoading = false;
    this.errorMessage = err.error?.message || 'Import failed. Please try again.';
    console.error('OPML Import Error:', err);
  }
}
