Feature: Actualizar progreso de proyecto
  Scenario: Entregable completado actualiza progreso del proyecto
    Given Soy una empresa
    And Tengo un proyecto con dos entregables
    When El desarrollador completa un entregable
    Then El progreso del proyecto se actualizara