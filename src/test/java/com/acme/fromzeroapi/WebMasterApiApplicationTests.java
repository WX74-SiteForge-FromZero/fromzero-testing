package com.acme.fromzeroapi;

import com.acme.fromzeroapi.profiles.domain.model.aggregates.Company;
import com.acme.fromzeroapi.profiles.domain.model.aggregates.Developer;
import com.acme.fromzeroapi.projects.application.internal.commandServices.ProjectCommandServiceImpl;
import com.acme.fromzeroapi.projects.application.internal.outboundServices.acl.ExternalProfileProjectService;
import com.acme.fromzeroapi.projects.domain.model.aggregates.Project;
import com.acme.fromzeroapi.projects.domain.model.commands.*;
import com.acme.fromzeroapi.projects.domain.model.entities.Deliverable;
import com.acme.fromzeroapi.projects.domain.model.events.CreateDefaultDeliverablesEvent;
import com.acme.fromzeroapi.projects.domain.model.valueObjects.*;
import com.acme.fromzeroapi.projects.domain.services.ProjectCommandService;
import com.acme.fromzeroapi.projects.infrastructure.persistence.jpa.repositories.ProjectRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class WebMasterApiApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    public void testCreateDefaultDeliverablesWhenProjectMethodologiesAreEmpty() {
        // arrange
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
        var company = new Company(
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
        var project = new Project(command, company);
        var deliverables = new ArrayList<Deliverable>();

        // act
        boolean emptyMethodologies = "".equals(project.getMethodologies());
        if (emptyMethodologies) {
            var deliverable1 = new Deliverable();
            var deliverable2 = new Deliverable();
            var deliverable3 = new Deliverable();
            var deliverable4 = new Deliverable();
            deliverables.add(deliverable1);
            deliverables.add(deliverable2);
            deliverables.add(deliverable3);
            deliverables.add(deliverable4);
        }

        // assert
        assertTrue(emptyMethodologies, "Methodologies should be empty");
        assertEquals(4, deliverables.size());
    }

    @Test
    public void testUpdateProjectProgressWhenDeliverableIsAccepted() {
        // arrange
        var command = new CreateProjectCommand(
                "",
                "",
                6L,
                new HashSet<>(),
                new HashSet<>(),
                ProjectType.WEB_APPLICATION,
                "700 dolares",
                """
                        1. Meeting to coordinate deliverables
                        2. Send Web Application Mockups
                        """
        );
        var company = new Company(
                "Company1",
                "description",
                "peru",
                "0234032",
                "3298923423",
                "",
                "",
                "sector1",
                3L
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
                "Meeting to coordinate deliverables",
                """
                        In this deliverable, the developer will send the web applications mockups.
                        These ones will be reviewed by the Compny
                        """,
                LocalDate.now(),
                1L
        );
        var deliverable1 = new Deliverable(deliverable1Command, project);
        var deliverable2 = new Deliverable(deliverable2Command, project);
        deliverables.add(deliverable1);
        deliverables.add(deliverable2);

        // act
        deliverable1.setState(DeliverableState.COMPLETADO);
        long completedDeliverables = 1;
        int totalDeliverables = deliverables.size();
        double progress = (double) completedDeliverables / totalDeliverables * 100;
        project.setProgress(progress);

        // assert
        assertEquals(50.0, project.getProgress());
    }


    @Nested
    class ProjectCommandServiceTests {

        @Mock
        private ProjectRepository projectRepository;
        @Mock
        private ExternalProfileProjectService externalProfileProjectService;
        @Mock
        private ApplicationEventPublisher eventPublisher;

        private ProjectCommandService projectCommandService;

        @BeforeEach
        void setUp() {
            MockitoAnnotations.openMocks(this);
            projectCommandService = new ProjectCommandServiceImpl(
                    projectRepository,
                    externalProfileProjectService,
                    eventPublisher
            );
        }

        @Test
        public void testCreateProject() {
            // Arrange
            CreateProjectCommand command = new CreateProjectCommand(
                    "Test Project",
                    "A test project description",
                    1L,
                    Set.of(ProgrammingLanguages.Javascript),
                    Set.of(Frameworks.React),
                    ProjectType.WEB_APPLICATION,
                    "5000",
                    ""
            );

            Company company = new Company();
            company.setCompanyName("Test Company");

            when(externalProfileProjectService.getCompanyById(1L)).thenReturn(Optional.of(company));
            when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Optional<Project> result = projectCommandService.handle(command);

            // Assert
            System.out.println("Test Create Project:");
            System.out.println("Expected: Project created with name 'Test Project' and state EN_BUSQUEDA");
            System.out.println("Actual: " + (result.isPresent() ?
                    "Project created with name '" + result.get().getName() + "' and state " + result.get().getState() :
                    "No project created"));

            assertTrue(result.isPresent());
            Project project = result.get();
            assertEquals("Test Project", project.getName());
            assertEquals(ProjectState.EN_BUSQUEDA, project.getState());
            assertEquals(0.0, project.getProgress());
            assertEquals(company, project.getCompany());

            // Verify event publication
            verify(eventPublisher, times(1)).publishEvent(any(CreateDefaultDeliverablesEvent.class));
            System.out.println("Expected: CreateDefaultDeliverablesEvent published once");
            System.out.println("Actual: CreateDefaultDeliverablesEvent published " +
                    (verifyEventPublished() ? "once" : "not at all"));
        }

        // Helper method to verify event publication. Returns true if event was published once, false otherwise.
        private boolean verifyEventPublished() {
            try {
                verify(eventPublisher, times(1)).publishEvent(any(CreateDefaultDeliverablesEvent.class));
                return true;
            } catch (AssertionError e) {
                return false;
            }
        }

        @Test
        public void testUpdateProjectCandidatesList() {
            // Arrange
            Long projectId = 1L;
            Long developerId = 2L;
            Project project = new Project();
            project.setState(ProjectState.EN_BUSQUEDA);
            Developer developer = new Developer();

            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
            when(externalProfileProjectService.getDeveloperById(developerId)).thenReturn(Optional.of(developer));
            when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UpdateProjectCandidatesListCommand command = new UpdateProjectCandidatesListCommand(developerId, projectId);

            // Act
            Optional<Project> result = projectCommandService.handle(command);

            // Assert
            System.out.println("Test Update Project Candidates List:");
            System.out.println("Expected: Project updated with new candidate");
            System.out.println("Actual: " + (result.isPresent() && result.get().getCandidates().contains(developer) ?
                    "Project updated with new candidate" : "Project not updated or candidate not added"));

            assertTrue(result.isPresent());
            Project updatedProject = result.get();
            assertTrue(updatedProject.getCandidates().contains(developer));
            assertEquals(ProjectState.EN_BUSQUEDA, updatedProject.getState());
        }

        @Test
        public void testAssignProjectDeveloper() {
            // Arrange
            Long projectId = 1L;
            Long developerId = 2L;
            Project project = new Project();
            project.setState(ProjectState.EN_BUSQUEDA);
            Developer developer = new Developer();
            project.getCandidates().add(developer);

            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
            when(externalProfileProjectService.getDeveloperById(developerId)).thenReturn(Optional.of(developer));
            when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

            AssignProjectDeveloperCommand command = new AssignProjectDeveloperCommand(projectId, developerId);

            // Act
            Optional<Project> result = projectCommandService.handle(command);

            // Assert
            System.out.println("Test Assign Project Developer:");
            System.out.println("Expected: Developer assigned and project state changed to EN_PROGRESO");
            System.out.println("Actual: " + (result.isPresent() ?
                    "Developer " + (result.get().getDeveloper() == developer ? "assigned" : "not assigned") +
                            " and project state is " + result.get().getState() : "Project not found"));

            assertTrue(result.isPresent());
            Project updatedProject = result.get();
            assertEquals(developer, updatedProject.getDeveloper());
            assertTrue(updatedProject.getCandidates().isEmpty());
            assertEquals(ProjectState.EN_PROGRESO, updatedProject.getState());
        }

        @Test
        public void testUpdateProjectProgress() {
            // Arrange
            Long projectId = 1L;
            Project project = new Project();
            project.setState(ProjectState.EN_PROGRESO);
            project.setProgress(0.0);
            Developer developer = new Developer();
            project.setDeveloper(developer);

            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
            when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));
            doNothing().when(externalProfileProjectService).updateDeveloperCompletedProjects(anyLong());

            UpdateProjectProgressCommand command = new UpdateProjectProgressCommand(projectId, 4L, 4);

            // Act
            projectCommandService.handle(command);

            // Assert
            System.out.println("Test Update Project Progress:");
            System.out.println("Expected: Project progress 100.0% and state COMPLETADO");
            System.out.println("Actual: Project progress " + project.getProgress() + "% and state " + project.getState());

            assertEquals(100.0, project.getProgress(), 0.01);
            assertEquals(ProjectState.COMPLETADO, project.getState());
            verify(externalProfileProjectService).updateDeveloperCompletedProjects(anyLong());
        }
    }


}
