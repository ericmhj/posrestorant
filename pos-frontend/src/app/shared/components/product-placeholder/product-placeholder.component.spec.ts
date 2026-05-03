import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  ProductPlaceholderComponent,
  generatePlaceholderColor,
  getContrastColor,
  getInitials
} from './product-placeholder.component';

describe('ProductPlaceholderComponent', () => {
  let component: ProductPlaceholderComponent;
  let fixture: ComponentFixture<ProductPlaceholderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductPlaceholderComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(ProductPlaceholderComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    component.productName = 'Tacos';
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should show img when imagenUrl is set', () => {
    component.productName = 'Tacos';
    component.imagenUrl = '/uploads/productos/test.jpg';
    fixture.detectChanges();
    const img = fixture.nativeElement.querySelector('img');
    expect(img).toBeTruthy();
    expect(img.src).toContain('test.jpg');
  });

  it('should show placeholder when imagenUrl is null', () => {
    component.productName = 'Tacos';
    component.imagenUrl = null;
    fixture.detectChanges();
    const placeholder = fixture.nativeElement.querySelector('.product-placeholder');
    expect(placeholder).toBeTruthy();
  });

  it('should show initials in placeholder', () => {
    component.productName = 'Tacos de Birria';
    component.imagenUrl = null;
    fixture.detectChanges();
    const text = fixture.nativeElement.querySelector('.placeholder-text');
    expect(text.textContent.trim()).toBe('TB');
  });

  it('should fall back to placeholder on image error', () => {
    component.productName = 'Tacos';
    component.imagenUrl = '/bad-url.jpg';
    fixture.detectChanges();
    component.onImageError();
    fixture.detectChanges();
    expect(component.imagenUrl).toBeNull();
  });
});

// -----------------------------------------------------------------------
// Property 19: generatePlaceholderColor is deterministic
// -----------------------------------------------------------------------
describe('generatePlaceholderColor — Property 19', () => {

  /**
   * Tag: Feature: devcontainer-setup, Property 19: Color de placeholder es determinista
   *
   * For any product name N, calling generatePlaceholderColor(N) twice
   * must return the identical color value both times.
   */
  it('Property 19: same name always returns same color (100 names)', () => {
    const names = [
      'Tacos', 'Burrito', 'Quesadilla', 'Enchiladas', 'Pozole',
      'Caldo de Res', 'Agua de Jamaica', 'Cerveza', 'Margarita', 'Mojito',
      'Ensalada César', 'Sopa de Lima', 'Cochinita Pibil', 'Chiles en Nogada',
      'Mole Negro', 'Tamales', 'Tostadas', 'Flautas', 'Sopes', 'Gorditas',
      'Chilaquiles', 'Huevos Rancheros', 'Machaca', 'Birria', 'Barbacoa',
      'Carnitas', 'Al Pastor', 'Suadero', 'Tripa', 'Cabeza',
      'Agua de Horchata', 'Agua de Tamarindo', 'Limonada', 'Naranjada',
      'Café de Olla', 'Té de Manzanilla', 'Chocolate Caliente', 'Atole',
      'Champurrado', 'Tepache', 'Michelada', 'Chelada', 'Paloma',
      'Tequila', 'Mezcal', 'Ron', 'Vodka', 'Whisky', 'Brandy', 'Ginebra',
      'Flan', 'Churros', 'Buñuelos', 'Arroz con Leche', 'Tres Leches',
      'Pastel de Chocolate', 'Helado de Vainilla', 'Nieve de Limón',
      'Gelatina', 'Fruta con Crema', 'Elote', 'Esquites', 'Jícama',
      'Pepino con Chile', 'Mango con Chamoy', 'Gaznates', 'Polvorones',
      'Empanadas', 'Gorditas de Chicharrón', 'Tlayudas', 'Memelas',
      'Tetelas', 'Tlacoyos', 'Huaraches', 'Volcanes', 'Gringas',
      'Mulitas', 'Vampiros', 'Campechanas', 'Tortas', 'Cemitas',
      'Pambazo', 'Molletes', 'Sincronizadas', 'Quesadillas de Flor',
      'Caldo Tlalpeño', 'Menudo', 'Pancita', 'Birria de Chivo',
      'Cabrito', 'Arrachera', 'Rib Eye', 'T-Bone', 'Costillas BBQ',
      'Pollo a la Plancha', 'Pescado a la Veracruzana', 'Camarones al Mojo',
      'Pulpo a las Brasas', 'Ceviche', 'Aguachile', 'Tostadas de Atún',
      'A', 'AB', 'ABC'
    ];

    // Ensure we have at least 100 names
    expect(names.length).toBeGreaterThanOrEqual(100);

    for (const name of names) {
      const color1 = generatePlaceholderColor(name);
      const color2 = generatePlaceholderColor(name);
      expect(color1).toBe(color2,
        `Color must be deterministic for name: "${name}". Got "${color1}" and "${color2}"`);
      // Must be a valid hex color
      expect(color1).toMatch(/^#[0-9a-f]{6}$/i,
        `Color must be a valid hex color for name: "${name}"`);
    }
  });

  it('empty name returns a valid color', () => {
    const color = generatePlaceholderColor('');
    expect(color).toMatch(/^#[0-9a-f]{6}$/i);
  });

  it('different names can produce different colors', () => {
    const colors = new Set(['Tacos', 'Cerveza', 'Flan', 'Pozole', 'Mole']
      .map(generatePlaceholderColor));
    // At least 2 different colors among 5 distinct names
    expect(colors.size).toBeGreaterThan(1);
  });
});

// -----------------------------------------------------------------------
// getContrastColor tests
// -----------------------------------------------------------------------
describe('getContrastColor', () => {
  it('returns dark text for light background', () => {
    expect(getContrastColor('#ffffff')).toBe('#1a1a1a');
    expect(getContrastColor('#f0f0f0')).toBe('#1a1a1a');
    expect(getContrastColor('#f39c12')).toBe('#1a1a1a');
  });

  it('returns white text for dark background', () => {
    expect(getContrastColor('#000000')).toBe('#ffffff');
    expect(getContrastColor('#2c3e50')).toBe('#ffffff');
    expect(getContrastColor('#e74c3c')).toBe('#ffffff');
  });
});

// -----------------------------------------------------------------------
// getInitials tests
// -----------------------------------------------------------------------
describe('getInitials', () => {
  it('returns first 2 chars for single word', () => {
    expect(getInitials('Tacos')).toBe('TA');
  });

  it('returns first letter of first two words', () => {
    expect(getInitials('Tacos de Birria')).toBe('TB');
    expect(getInitials('Agua Jamaica')).toBe('AJ');
  });

  it('returns ? for empty name', () => {
    expect(getInitials('')).toBe('?');
  });
});
