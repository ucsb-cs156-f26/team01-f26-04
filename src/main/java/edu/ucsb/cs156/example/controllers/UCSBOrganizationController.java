package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for UCSBOrganization */
@Tag(name = "UCSBOrganization")
@RequestMapping("/api/ucsborganization")
@RestController
@Slf4j
public class UCSBOrganizationController extends ApiController {

  @Autowired UCSBOrganizationRepository ucsbOrganizationRepository;

  /**
   * This method returns a list of all ucsb organizations.
   *
   * @return a list of all ucsb organizations
   */
  @Operation(summary = "List all ucsb organizations")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<UCSBOrganization> allOrganizations() {
    Iterable<UCSBOrganization> orgs = ucsbOrganizationRepository.findAll();
    return orgs;
  }

  /**
   * This method returns a single ucsb organization.
   *
   * @param orgCode code of the organization
   * @return a single ucsb organization
   */
  @Operation(summary = "Get a single ucsb organization")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public UCSBOrganization getById(@Parameter(name = "orgCode") @RequestParam String orgCode) {
    UCSBOrganization org =
        ucsbOrganizationRepository
            .findById(orgCode)
            .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, orgCode));

    return org;
  }

  /**
   * This method creates a new ucsb organization. Accessible only to users with the role
   * "ROLE_ADMIN".
   *
   * @param orgCode code of the organization
   * @param orgTranslationShort short name of the organization
   * @param orgTranslation full name of the organization
   * @param inactive whether or not the organization is inactive
   * @return the saved ucsb organization
   */
  @Operation(summary = "Create a new ucsb organization")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public UCSBOrganization postOrganization(
      @Parameter(name = "orgCode") @RequestParam String orgCode,
      @Parameter(name = "orgTranslationShort") @RequestParam String orgTranslationShort,
      @Parameter(name = "orgTranslation") @RequestParam String orgTranslation,
      @Parameter(name = "inactive") @RequestParam boolean inactive) {

    UCSBOrganization org = new UCSBOrganization();
    org.setOrgCode(orgCode);
    org.setOrgTranslationShort(orgTranslationShort);
    org.setOrgTranslation(orgTranslation);
    org.setInactive(inactive);

    UCSBOrganization savedOrg = ucsbOrganizationRepository.save(org);

    return savedOrg;
  }

  /**
   * Update a single ucsb organization. Accessible only to users with the role "ROLE_ADMIN".
   *
   * @param orgCode code of the organization
   * @param incoming the new organization contents
   * @return the updated organization object
   */
  @Operation(summary = "Update a single ucsb organization")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public UCSBOrganization updateOrganization(
      @Parameter(name = "orgCode") @RequestParam String orgCode,
      @RequestBody @Valid UCSBOrganization incoming) {

    UCSBOrganization org =
        ucsbOrganizationRepository
            .findById(orgCode)
            .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, orgCode));

    org.setOrgTranslationShort(incoming.getOrgTranslationShort());
    org.setOrgTranslation(incoming.getOrgTranslation());
    org.setInactive(incoming.getInactive());

    ucsbOrganizationRepository.save(org);

    return org;
  }
}
