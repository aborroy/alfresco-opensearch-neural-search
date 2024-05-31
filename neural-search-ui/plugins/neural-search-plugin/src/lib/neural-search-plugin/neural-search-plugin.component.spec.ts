import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NeuralSearchPluginComponent } from './neural-search-plugin.component';

describe('NeuralSearchPluginComponent', () => {
  let component: NeuralSearchPluginComponent;
  let fixture: ComponentFixture<NeuralSearchPluginComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NeuralSearchPluginComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(NeuralSearchPluginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
