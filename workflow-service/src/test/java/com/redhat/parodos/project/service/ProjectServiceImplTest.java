package com.redhat.parodos.project.service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.redhat.parodos.config.ModelMapperConfig;
import com.redhat.parodos.project.dto.ProjectRequestDTO;
import com.redhat.parodos.project.dto.ProjectResponseDTO;
import com.redhat.parodos.project.entity.Project;
import com.redhat.parodos.project.entity.ProjectUserRole;
import com.redhat.parodos.project.entity.Role;
import com.redhat.parodos.project.repository.ProjectRepository;
import com.redhat.parodos.project.repository.ProjectUserRoleRepository;
import com.redhat.parodos.project.repository.RoleRepository;
import com.redhat.parodos.user.entity.User;
import com.redhat.parodos.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.modelmapper.ModelMapper;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class ProjectServiceImplTest {

	private final static String TEST_PROJECT_NAME = "test-project-name";

	private final static String TEST_PROJECT_DESCRIPTION = "test-project-description";

	private final static String TEST_PROJECT_ONE_NAME = "test-project-one-name";

	private final static String TEST_PROJECT_ONE_DESCRIPTION = "test-project-one-description";

	private final static String TEST_PROJECT_TWO_NAME = "test-project-two-name";

	private final static String TEST_PROJECT_TWO_DESCRIPTION = "test-project-two-description";

	private final static String TEST_USERNAME = "test-username";

	private final static UUID TEST_USER_ID = UUID.randomUUID();

	private final static String TEST_ROLE_OWNER_NAME = com.redhat.parodos.project.enums.Role.OWNER.name();

	@Mock
	private ProjectRepository projectRepository;

	@Mock
	private RoleRepository roleRepository;

	@Mock
	private ProjectUserRoleRepository projectUserRoleRepository;

	@Mock
	private UserService userService;

	@Spy
	private ModelMapper modelMapper = new ModelMapperConfig().modelMapper();

	private ProjectServiceImpl projectService;

	@BeforeEach
	public void initEach() {
		this.projectService = new ProjectServiceImpl(this.projectRepository, this.roleRepository,
				this.projectUserRoleRepository, this.userService, this.modelMapper);
	}

	@Test
	public void testGetProjectByIdWithValidData() {
		// given
		Project project = getProject(TEST_PROJECT_NAME, TEST_PROJECT_DESCRIPTION);
		when(this.projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

		// when
		ProjectResponseDTO projectResponseDTO = this.projectService.getProjectById(project.getId());

		// then
		assertNotNull(projectResponseDTO);
		assertEquals(projectResponseDTO.getId(), project.getId());
	}

	@Test
	public void testGetProjectByIdAndUsernameWithValidData() {
		// given
		User user = getUser(TEST_USER_ID, TEST_USERNAME);
		Role role = getRole(TEST_ROLE_OWNER_NAME);
		Project project = getProject(TEST_PROJECT_NAME, TEST_PROJECT_DESCRIPTION);
		ProjectUserRole projectUserRole = getProjectUserRole(project, user, role);

		// when
		when(this.userService.getUserEntityByUsername(TEST_USERNAME)).thenReturn(user);
		when(this.projectUserRoleRepository.findByProjectIdAndUserId(project.getId(), TEST_USER_ID))
				.thenReturn(List.of(projectUserRole));

		List<ProjectResponseDTO> projectResponseDTOs = this.projectService.getProjectByIdAndUsername(project.getId(),
				TEST_USERNAME);

		// then
		assertNotNull(projectResponseDTOs);
		assertEquals(1, projectResponseDTOs.size());
		assertEquals(projectResponseDTOs.get(0).getId(), project.getId());
	}

	@Test
	public void testFindProjectByIdWithInvalidData() {
		// given
		Project project = getProject(TEST_PROJECT_NAME, TEST_PROJECT_DESCRIPTION);

		// when
		when(this.projectRepository.findById(project.getId())).thenReturn(Optional.empty());

		// then
		assertThrows(ResponseStatusException.class, () -> this.projectService.getProjectById(project.getId()),
				String.format("404 NOT_FOUND \"Project with id: %s not found\"", project.getId()));
	}

	@Test
	@WithMockUser(username = "test-user")
	public void testGetProjectsWithValidData() {
		// given
		User user = getUser(TEST_USER_ID, TEST_USERNAME);
		Role role = getRole(TEST_ROLE_OWNER_NAME);
		Project projectOne = getProject(TEST_PROJECT_ONE_NAME, TEST_PROJECT_ONE_DESCRIPTION);
		Project projectTwo = getProject(TEST_PROJECT_TWO_NAME, TEST_PROJECT_TWO_DESCRIPTION);

		// when
		when(this.userService.getUserEntityByUsername(nullable(String.class))).thenReturn(user);
		when(this.projectUserRoleRepository.findByUserId(TEST_USER_ID)).thenReturn(
				List.of(getProjectUserRole(projectOne, user, role), getProjectUserRole(projectTwo, user, role)));

		List<ProjectResponseDTO> projectResponseDTOs = this.projectService.getProjects();

		// then
		assertNotNull(projectResponseDTOs);
		assertEquals(projectResponseDTOs.size(), 2);
		assertEquals(projectResponseDTOs.get(0).getName(), TEST_PROJECT_ONE_NAME);
		assertEquals(projectResponseDTOs.get(1).getName(), TEST_PROJECT_TWO_NAME);
	}

	@Test
	@WithMockUser(username = TEST_USERNAME)
	public void testGetProjectsWithInvalidData() {
		// when
		when(this.userService.getUserEntityByUsername(nullable(String.class)))
				.thenReturn(getUser(TEST_USER_ID, TEST_USERNAME));
		when(this.projectUserRoleRepository.findByUserId(TEST_USER_ID)).thenReturn(Collections.emptyList());

		List<ProjectResponseDTO> projectResponseDTOs = this.projectService.getProjects();

		// then
		assertNotNull(projectResponseDTOs);
		assertEquals(projectResponseDTOs.size(), 0);
	}

	@Test
	@WithMockUser(username = TEST_USERNAME)
	public void testSaveWithValidData() {
		// given
		User user = getUser(TEST_USER_ID, TEST_USERNAME);
		Role role = getRole(TEST_ROLE_OWNER_NAME);
		Project project = getProject(TEST_PROJECT_NAME, TEST_PROJECT_DESCRIPTION);
		ProjectUserRole projectUserRole = ProjectUserRole.builder().project(project).user(user).role(role).build();
		ProjectRequestDTO projectRequestDTO = ProjectRequestDTO.builder().name(TEST_PROJECT_NAME)
				.description(TEST_PROJECT_DESCRIPTION).build();

		// when
		when(this.userService.getUserEntityByUsername(nullable(String.class))).thenReturn(user);
		when(this.roleRepository.findByNameIgnoreCase(nullable(String.class))).thenReturn(Optional.ofNullable(role));
		when(this.projectRepository.save(any(Project.class))).thenReturn(project);
		when(this.projectUserRoleRepository.save(any(ProjectUserRole.class))).thenReturn(projectUserRole);

		ProjectResponseDTO projectResponseDTO = this.projectService.save(projectRequestDTO);

		// then
		ArgumentCaptor<Project> projectArgumentCaptor = ArgumentCaptor.forClass(Project.class);
		verify(this.projectRepository, times(1)).save(projectArgumentCaptor.capture());

		ArgumentCaptor<ProjectUserRole> projectUserRoleArgumentCaptor = ArgumentCaptor.forClass(ProjectUserRole.class);
		verify(this.projectUserRoleRepository, times(1)).save(projectUserRoleArgumentCaptor.capture());

		assertEquals(projectArgumentCaptor.getValue().getDescription(), TEST_PROJECT_DESCRIPTION);
		assertEquals(projectArgumentCaptor.getValue().getName(), TEST_PROJECT_NAME);
		assertNotNull(projectResponseDTO);
		assertEquals(projectResponseDTO.getId(), project.getId());
	}

	private Project getProject(String name, String description) {
		Project project = Project.builder().name(name).description(description).createDate(new Date())
				.modifyDate(new Date()).build();
		project.setId(UUID.randomUUID());
		return project;
	}

	private User getUser(UUID userId, String username) {
		User user = User.builder().username(username).build();
		user.setId(userId);
		return user;
	}

	private Role getRole(String roleName) {
		return Role.builder().name(roleName).build();
	}

	private ProjectUserRole getProjectUserRole(Project project, User user, Role role) {
		return ProjectUserRole.builder().project(project).user(user).role(role).build();
	}

}
