Feature: Crear entregables
  Scenario: Empresa elige metodologias por defecto
    Given Soy una empresa
    And Quiero crear un proyecto
    When Ingrese metodologias en blanco
    Then Se van a crear cuatro entregables por defecto