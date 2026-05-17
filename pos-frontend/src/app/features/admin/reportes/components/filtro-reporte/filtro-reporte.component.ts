import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReporteService } from '../../services/reporte.service';
import { PeriodoFiltro, PeriodoFiltroHelper } from '../../models/periodo-filtro.model';
import { TURNOS, UsuarioDTO } from '../../models/turno.model';

@Component({
  selector: 'app-filtro-reporte',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './filtro-reporte.component.html',
  styleUrls: ['./filtro-reporte.component.scss']
})
export class FiltroReporteComponent implements OnInit {

  @Output() onFilter = new EventEmitter<PeriodoFiltro>();

  filtro: PeriodoFiltro = PeriodoFiltroHelper.crearUltimos30Dias();
  turnos = TURNOS;
  meseros: UsuarioDTO[] = [];
  cargando = false;

  presets = [
    { label: 'Hoy', valor: 'hoy' },
    { label: 'Esta Semana', valor: 'semana' },
    { label: 'Este Mes', valor: 'mes' },
    { label: 'Últimos 30 días', valor: '30dias' }
  ];

  constructor(private reporteService: ReporteService) {}

  ngOnInit(): void {
    this.cargarFiltros();
  }

  cargarFiltros(): void {
    this.cargando = true;
    this.reporteService.getMeseros().subscribe({
      next: (meseros: UsuarioDTO[]) => {
        this.meseros = meseros;
        this.cargando = false;
      },
      error: (err: any) => {
        console.error('Error cargando meseros:', err);
        this.cargando = false;
      }
    });
  }

  aplicarPreset(preset: string): void {
    switch (preset) {
      case 'hoy':
        this.filtro = PeriodoFiltroHelper.crearHoy();
        break;
      case 'semana':
        this.filtro = PeriodoFiltroHelper.crearEstaSemana();
        break;
      case 'mes':
        this.filtro = PeriodoFiltroHelper.crearEsteMes();
        break;
      case '30dias':
        this.filtro = PeriodoFiltroHelper.crearUltimos30Dias();
        break;
    }
  }

  aplicarFiltros(): void {
    if (!this.reporteService.validarFiltro(this.filtro)) {
      alert('Por favor, selecciona un rango de fechas válido (desde < hasta)');
      return;
    }
    this.onFilter.emit(this.filtro);
  }

  limpiarFiltros(): void {
    this.filtro = {
      desde: new Date(),
      hasta: new Date(),
      turno: undefined,
      usuarioId: undefined
    };
  }

  get fechaDesdeFormato(): string {
    return this.reporteService.formatearFecha(this.filtro.desde);
  }

  set fechaDesdeFormato(value: string) {
    this.filtro.desde = new Date(value);
  }

  get fechaHastaFormato(): string {
    return this.reporteService.formatearFecha(this.filtro.hasta);
  }

  set fechaHastaFormato(value: string) {
    this.filtro.hasta = new Date(value);
  }

  get meseroSeleccionado(): UsuarioDTO | undefined {
    return this.meseros.find(m => m.id === this.filtro.usuarioId);
  }
}
