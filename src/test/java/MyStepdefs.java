import com.acme.fromzeroapi.profiles.domain.model.aggregates.Company;
import com.acme.fromzeroapi.projects.domain.model.aggregates.Project;
import com.acme.fromzeroapi.projects.domain.model.commands.CreateDeliverableCommand;
import com.acme.fromzeroapi.projects.domain.model.commands.CreateProjectCommand;
import com.acme.fromzeroapi.projects.domain.model.entities.Deliverable;
import com.acme.fromzeroapi.projects.domain.model.valueObjects.DeliverableState;
import com.acme.fromzeroapi.projects.domain.model.valueObjects.ProjectType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;

public class MyStepdefs {

    // Feature:Crear entregables
    // Scenario:Empresa elige metodologias por defecto
    @Given("Soy una empresa")
    public Company createSoyUnaEmpresa() {

        return new Company(
                "Company1",
                "description",
                "peru",
                "0234032",
                "3298923423",
                "",
                "",
                "sector1",
                2L
        );
    }

    @And("Quiero crear un proyecto")
    public Project createQuieroCrearUnProyecto(Company company) {

        var command = new CreateProjectCommand(
                "",
                "",
                6L,
                new HashSet<>(),
                new HashSet<>(),
                ProjectType.LANDING_PAGE,
                "500 dolares",
                """
                        1. Meeting to coordinate deliverables
                        """
        );
        return new Project(command, company);
    }

    @When("Ingrese metodologias en blanco")
    public boolean verifyMethodologies(Project project) {
        return "".equals(project.getMethodologies());
    }

    @Then("Se van a crear cuatro entregables por defecto")
    public int getDeliverablesSize(ArrayList<Deliverable> deliverables) {
        return deliverables.size();
    }

    // Feature:
    // Scenario:
    @Given("Soy una empresa")
    public Company createSoyUnaEmpresa2() {
        return new Company(
                "Company1", "description", "peru",
                "0234032", "3298923423", "", "", "sector1", 3L
        );
    }

    @And("Tengo un proyecto con dos entregables")
    public Project createProjectWithTwoDeliverables(Company company) {
        var command = new CreateProjectCommand(
                "", "", 6L, new HashSet<>(),
                new HashSet<>(),
                ProjectType.WEB_APPLICATION,
                "700 dolares",
                """
                        1. Meeting to coordinate deliverables
                        2. Send Web Application Mockups
                        """
        );
        var project = new Project(command, company);
        var deliverables = new ArrayList<Deliverable>();
        var deliverable1Command = new CreateDeliverableCommand(
                "Meeting to coordinate deliverables",
                """
                        In this deliverable, there will be a meeting with the developer
                        """,
                LocalDate.now(),
                1L
        );
        var deliverable2Command = new CreateDeliverableCommand(
                "Send Web Application Mockups",
                """
                        In this deliverable, the developer will send the web applications mockups.
                        These ones will be reviewed by the Company
                        """,
                LocalDate.now(), 1L
        );
        var deliverable1 = new Deliverable(deliverable1Command, project);
        var deliverable2 = new Deliverable(deliverable2Command, project);
        deliverables.add(deliverable1);
        deliverables.add(deliverable2);
        return project;
    }

    @When("El desarrollador completa un entregable")
    public double completeDeliverable(ArrayList<Deliverable> deliverables, Deliverable deliverable) {
        deliverable.setState(DeliverableState.COMPLETADO);
        long completedDeliverables = 1;
        int totalDeliverables = deliverables.size();
        double progress = (double) completedDeliverables / totalDeliverables * 100;
        return progress;
    }

    @Then("El progreso del proyecto se actualizara")
    public void updateProjectProgress(double progress, Project project) {
        project.setProgress(progress);
    }
}
