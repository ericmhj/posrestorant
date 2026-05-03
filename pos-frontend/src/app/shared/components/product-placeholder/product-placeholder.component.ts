import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';

/**
 * Displays a product image or a deterministic color placeholder with the
 * product name when no image is available.
 *
 * The placeholder color is generated deterministically from the product name
 * using the djb2 hash algorithm, so the same product always shows the same
 * color across all clients (Property 19).
 */
@Component({
  selector: 'app-product-placeholder',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="product-image-container" [style.width]="size" [style.height]="size">
      <img
        *ngIf="imagenUrl; else placeholder"
        [src]="imagenUrl"
        [alt]="productName"
        class="product-image"
        (error)="onImageError()"
      />
      <ng-template #placeholder>
        <div
          class="product-placeholder"
          [style.background-color]="placeholderColor"
          [style.color]="textColor"
          [attr.aria-label]="productName"
          role="img"
        >
          <span class="placeholder-text">{{ initials }}</span>
        </div>
      </ng-template>
    </div>
  `,
  styles: [`
    .product-image-container {
      display: inline-block;
      overflow: hidden;
      border-radius: 8px;
      flex-shrink: 0;
    }

    .product-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }

    .product-placeholder {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 8px;
      user-select: none;
    }

    .placeholder-text {
      font-weight: 600;
      text-align: center;
      padding: 4px;
      overflow: hidden;
      word-break: break-word;
      line-height: 1.2;
      font-size: clamp(0.6rem, 2.5cqi, 1.1rem);
    }
  `]
})
export class ProductPlaceholderComponent implements OnChanges {

  @Input() productName: string = '';
  @Input() imagenUrl: string | null = null;
  @Input() size: string = '80px';

  placeholderColor: string = '#6c757d';
  textColor: string = '#ffffff';
  initials: string = '';
  imageError = false;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['productName'] || changes['imagenUrl']) {
      this.imageError = false;
      this.placeholderColor = generatePlaceholderColor(this.productName);
      this.textColor = getContrastColor(this.placeholderColor);
      this.initials = getInitials(this.productName);
    }
  }

  onImageError(): void {
    this.imageError = true;
    this.imagenUrl = null;
  }
}

// -----------------------------------------------------------------------
// Pure functions — exported for testing (Property 19)
// -----------------------------------------------------------------------

/**
 * Generates a deterministic background color from a product name.
 * Uses the djb2 hash algorithm to map the name to one of 12 accessible colors.
 * Same name always returns the same color on all clients.
 */
export function generatePlaceholderColor(name: string): string {
  if (!name || name.trim() === '') return PALETTE[0];

  let hash = 5381;
  for (let i = 0; i < name.length; i++) {
    hash = ((hash << 5) + hash) + name.charCodeAt(i);
    hash = hash & hash; // Convert to 32-bit integer
  }

  const index = Math.abs(hash) % PALETTE.length;
  return PALETTE[index];
}

/**
 * Returns white or dark text color based on background luminance (WCAG contrast).
 */
export function getContrastColor(hexColor: string): string {
  const r = parseInt(hexColor.slice(1, 3), 16);
  const g = parseInt(hexColor.slice(3, 5), 16);
  const b = parseInt(hexColor.slice(5, 7), 16);
  // Relative luminance formula (WCAG 2.1)
  const luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255;
  return luminance > 0.5 ? '#1a1a1a' : '#ffffff';
}

/**
 * Extracts up to 2 initials from a product name for the placeholder text.
 */
export function getInitials(name: string): string {
  if (!name || name.trim() === '') return '?';
  const words = name.trim().split(/\s+/);
  if (words.length === 1) {
    return words[0].substring(0, 2).toUpperCase();
  }
  return (words[0][0] + words[1][0]).toUpperCase();
}

/**
 * 12 accessible background colors for product placeholders.
 * All chosen to provide sufficient contrast with both white and dark text.
 */
const PALETTE: string[] = [
  '#e74c3c', // red
  '#e67e22', // orange
  '#f39c12', // yellow-orange
  '#27ae60', // green
  '#16a085', // teal
  '#2980b9', // blue
  '#8e44ad', // purple
  '#2c3e50', // dark blue-grey
  '#c0392b', // dark red
  '#d35400', // dark orange
  '#1abc9c', // turquoise
  '#7f8c8d', // grey
];
