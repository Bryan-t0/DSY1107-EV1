package cl.duoc.pedidos360.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

// esta clase representa un pedido en la base de datos
@Entity
public class Pedido {

  // identificador del pedido
  @Id
  // el id se genera automaticamente
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String descripcion;
  // todos los pedidos parten como pendientes
  private String estado = "PENDIENTE";

  public Pedido() {
  }

  public Pedido(String descripcion) {
    this.descripcion = descripcion;
  }

  public Long getId() {
    return id;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }
}
