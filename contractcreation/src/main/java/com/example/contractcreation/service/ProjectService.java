package com.example.contractcreation.service;

import com.example.contractcreation.Repository.ProjectRepository;
import com.example.contractcreation.enums.ProjectStatus;
import com.example.contractcreation.model.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {
    @Autowired
    private ProjectRepository projectRepository;

    public Project createProject(Project project) {
        project.setStatus(ProjectStatus.PLANNED);
        return projectRepository.save(project);
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public Project getProjectById(Long id) {
        return projectRepository.findById(id).orElse(null);
    }

    public Project updateProject(Long id, Project project) {
        Project existingProject = projectRepository.findById(id).orElse(null);

        if (existingProject != null) {
            existingProject.setName(project.getName());
            existingProject.setLocation(project.getLocation());
            existingProject.setBudget(project.getBudget());
            existingProject.setStatus(project.getStatus());
            return projectRepository.save(existingProject);
        }

        return null;
    }

    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }

    public Project updateStatus(Long id, String status) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        ProjectStatus current = project.getStatus();
        ProjectStatus newStatus = ProjectStatus.valueOf(status);

        // Validate transition
        if (!isValidTransition(current, newStatus)) {
            throw new RuntimeException(
                    "Invalid status transition from " + current + " to " + newStatus
            );
        }

        project.setStatus(newStatus);
        return projectRepository.save(project);
    }

    private boolean isValidTransition(ProjectStatus current, ProjectStatus next) {

        switch (current) {

            case PLANNED:
                return next == ProjectStatus.ACTIVE || next == ProjectStatus.CANCELLED;

            case ACTIVE:
                return next == ProjectStatus.ON_HOLD
                        || next == ProjectStatus.COMPLETED
                        || next == ProjectStatus.CANCELLED;

            case ON_HOLD:
                return next == ProjectStatus.ACTIVE
                        || next == ProjectStatus.CANCELLED;

            case COMPLETED:
            case CANCELLED:
                return false; // End states (no changes allowed)

            default:
                return false;
        }
    }

}
