package edu.ucsb.cs156.example.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A menu item offered at a UCSB dining commons. */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "ucsbdiningcommonsmenuitems")
public class UCSBDiningCommonsMenuItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  private String diningCommonsCode;
  private String name;
  private String station;
}
