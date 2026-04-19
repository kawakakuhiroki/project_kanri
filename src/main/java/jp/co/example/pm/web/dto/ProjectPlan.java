/*
 * [役割] ProjectPlan: プロジェクト計画の集約DTO。
 * [入力] APIからのJSON。
 * [出力] APIレスポンス。
 * [依存] Model。
 */
package jp.co.example.pm.web.dto;

import java.util.ArrayList;
import java.util.List;
import jp.co.example.pm.model.Dependency;
import jp.co.example.pm.model.Project;
import jp.co.example.pm.model.Resource;
import jp.co.example.pm.model.Task;
import jp.co.example.pm.model.TaskAssignment;

/**
 * [目的] ProjectPlan: プロジェクト計画の集約DTO。
 * [入力] APIからのJSON。
 * [出力] APIレスポンス。
 * [影響] 副作用なし。
 */
public class ProjectPlan {
  private Project project;
  private List<Task> tasks = new ArrayList<>();
  private List<Dependency> dependencies = new ArrayList<>();
  private List<Resource> resources = new ArrayList<>();
  private List<TaskAssignment> assignments = new ArrayList<>();

  public Project getProject() {
    return project;
  }

  public void setProject(Project project) {
    this.project = project;
  }

  public List<Task> getTasks() {
    return tasks;
  }

  public void setTasks(List<Task> tasks) {
    this.tasks = tasks == null ? new ArrayList<>() : new ArrayList<>(tasks);
  }

  public List<Dependency> getDependencies() {
    return dependencies;
  }

  public void setDependencies(List<Dependency> dependencies) {
    this.dependencies = dependencies == null ? new ArrayList<>() : new ArrayList<>(dependencies);
  }

  public List<Resource> getResources() {
    return resources;
  }

  public void setResources(List<Resource> resources) {
    this.resources = resources == null ? new ArrayList<>() : new ArrayList<>(resources);
  }

  public List<TaskAssignment> getAssignments() {
    return assignments;
  }

  public void setAssignments(List<TaskAssignment> assignments) {
    this.assignments = assignments == null ? new ArrayList<>() : new ArrayList<>(assignments);
  }
}
