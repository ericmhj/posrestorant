export type Turno = 'ALMUERZO' | 'CENA' | 'NOCHE' | '';

export const TURNOS: { value: Turno; label: string }[] = [
  { value: '', label: 'Todos los turnos' },
  { value: 'ALMUERZO', label: 'Almuerzo (11:00 - 15:00)' },
  { value: 'CENA', label: 'Cena (17:00 - 23:00)' },
  { value: 'NOCHE', label: 'Noche (23:00 - 11:00)' }
];

export interface UsuarioDTO {
  id: string;
  nombre: string;
  email: string;
  rol: string;
}
