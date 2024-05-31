import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SearchResult } from '../types';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { NeuralSearchService } from '../services/neural-search.service';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';

export interface SearchResultTableElement {
  id: string;
  name: string;
  text: string;
}

@Component({
  selector: 'lib-neural-search-plugin',
  standalone: true,
  imports: [
    CommonModule,
    MatInputModule,
    MatButtonModule,
    MatFormFieldModule,
    FormsModule,
    MatIconModule,
    MatTableModule,
    MatProgressSpinnerModule,
    MatSelectModule
  ],
  templateUrl: './neural-search-plugin.component.html',
  styleUrls: ['./neural-search-plugin.component.css']
})
export class NeuralSearchPluginComponent {
  private _results: SearchResult[] = [];
  displayedColumns: string[] = ['id', 'nameText'];
  loading = false;
  header = '';
  selectedSearchType: 'Semantic' | 'Keywords' | 'Hybrid' = 'Semantic';

  queryString = '';

  onSearch() {
    this.loading = true;
    this.searchService.searchFor(this.queryString, this.selectedSearchType).subscribe({
      next: (res) => {
        this._results = res;
        this.loading = false;
        this.header = this.queryString;
      }
    });
  }

  searchResults(): SearchResultTableElement[] {
    return this._results.map((e) => ({ id: e.uuid, name: e.name, text: e.text }));
  }

  replaceUnicode(text: string): string {
    const unicodeRegex = /\\u([\dA-Fa-f]{4})/g;
    return text.replace(unicodeRegex, (match, grp) => {
      return String.fromCharCode(parseInt(grp, 16));
    });
  }

  constructor(private searchService: NeuralSearchService) {}
}
