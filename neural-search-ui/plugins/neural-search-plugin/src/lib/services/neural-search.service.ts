import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { SearchResult } from '../types';
import { Observable } from 'rxjs';

const SEARCH_URL = 'http://localhost:4200/search';

@Injectable({
  providedIn: 'root'
})
export class NeuralSearchService {
  constructor(private http: HttpClient) {}

  searchFor(query: string, mode: 'Semantic' | 'Keywords' | 'Hybrid'): Observable<SearchResult[]> {
    const searchMode = this.resolveSearchMode(mode);
    const params = this.buildSearchParams(query, searchMode);
    return this.http.get<SearchResult[]>(SEARCH_URL, { params });
  }

  private resolveSearchMode(mode: string): string {
    switch (mode) {
      case 'Semantic':
        return 'neural';
      case 'Keywords':
        return 'keyword';
      case 'Hybrid':
        return 'hybrid';
      default:
        throw new Error(`Invalid search mode: ${mode}`);
    }
  }

  private buildSearchParams(query: string, mode: string): HttpParams {
    let params = new HttpParams();
    if (query) {
      params = params.set('query', query);
    }
    params = params.set('searchType', mode);
    return params;
  }
}
