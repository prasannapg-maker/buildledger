package com.example.contractcreation.service;

import com.example.contractcreation.Repository.ProjectRepository;
import com.example.contractcreation.model.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {
    @Autowired
    private ProjectRepository projectRepository;

    public Project createProject(Project project) {
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
}
