package com.example.arashco.controller;

import com.example.arashco.model.Employee;
import com.example.arashco.repository.EmployeeRepository;
import com.example.arashco.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class EmployeeControllerTest {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_PHONE = "0000000000";
    private static final String UPDATED_PHONE = "1111111111";

    private static final String DEFAULT_JOB_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_JOB_TITLE = "BBBBBBBBBB";

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeService employeeService;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext applicationContext;

    private Employee employee;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
        employee = createEntity();
    }

    private Employee createEntity() {
        Employee employee = new Employee();
        employee.setName(DEFAULT_NAME);
        employee.setPhone(DEFAULT_PHONE);
        employee.setJobTitle(DEFAULT_JOB_TITLE);
        return employee;
    }

    private void updateEntity(Employee employee) {
        employee.setPhone(UPDATED_PHONE);
        employee.setName(UPDATED_NAME);
        employee.setJobTitle(UPDATED_JOB_TITLE);
    }

    @Test
    @Transactional
    public void insertEmployeeTest() throws Exception {
        int sizeBeforeInsert = employeeService.findAllEmployees().size();

        ObjectMapper mapper = new ObjectMapper();
        String jsonEmployee = mapper.writeValueAsString(employee);

        mockMvc.perform(MockMvcRequestBuilders.post("/employee/add")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .content(jsonEmployee))
                .andExpect(status().isCreated());

        List<Employee> leadList = employeeService.findAllEmployees();

        assertThat(leadList).hasSize(sizeBeforeInsert + 1);
        Employee testLead = leadList.get(leadList.size() - 1);
        assertThat(testLead.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testLead.getJobTitle()).isEqualTo(DEFAULT_JOB_TITLE);
        assertThat(testLead.getPhone()).isEqualTo(DEFAULT_PHONE);
    }

    @Test
    @Transactional
    public void findAllEmployeeTest() throws Exception {
        int sizeBeforeInsert = employeeService.findAllEmployees().size();

        employee = employeeService.addEmployee(employee);

        mockMvc.perform(MockMvcRequestBuilders.get("/employee/all")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(sizeBeforeInsert + 1)))
                .andExpect(jsonPath("$[" + (sizeBeforeInsert) +"].id").value(employee.getId()))
                .andExpect(jsonPath("$[" + (sizeBeforeInsert) +"].phone").value(DEFAULT_PHONE))
                .andExpect(jsonPath("$[" + (sizeBeforeInsert) +"].jobTitle").value(DEFAULT_JOB_TITLE))
                .andExpect(jsonPath("$[" + (sizeBeforeInsert) +"].name").value(DEFAULT_NAME));
    }

    @Test
    @Transactional
    public void findEmployeeTest() throws Exception {
        employee = employeeService.addEmployee(employee);

        mockMvc.perform(MockMvcRequestBuilders.get("/employee/find/" + employee.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(employee.getId()))
                .andExpect(jsonPath("$.phone").value(DEFAULT_PHONE))
                .andExpect(jsonPath("$.jobTitle").value(DEFAULT_JOB_TITLE))
                .andExpect(jsonPath("$.name").value(DEFAULT_NAME));
    }

    @Test
    @Transactional
    public void updateEmployeeTest() throws Exception {
        employee = employeeService.addEmployee(employee);

        int sizeAfterInsert = employeeService.findAllEmployees().size();

        updateEntity(employee);

        ObjectMapper mapper = new ObjectMapper();
        String jsonEmployee = mapper.writeValueAsString(employee);

        mockMvc.perform(MockMvcRequestBuilders.put("/employee/update")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .content(jsonEmployee))
                .andExpect(status().isOk());

        List<Employee> leadList = employeeService.findAllEmployees();

        assertThat(leadList).hasSize(sizeAfterInsert);
        Employee testLead = leadList.get(leadList.size() - 1);
        assertThat(testLead.getId()).isEqualTo(employee.getId());
        assertThat(testLead.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testLead.getJobTitle()).isEqualTo(UPDATED_JOB_TITLE);
        assertThat(testLead.getPhone()).isEqualTo(UPDATED_PHONE);
    }

    @Test
    @Transactional
    public void deleteEmployeeTest() throws Exception {
        employee = employeeService.addEmployee(employee);

        int sizeAfterInsert = employeeService.findAllEmployees().size();

        mockMvc.perform(MockMvcRequestBuilders.delete("/employee/delete/" + employee.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent());

        List<Employee> leadList = employeeService.findAllEmployees();
        assertThat(leadList).hasSize(sizeAfterInsert - 1);
    }
}
