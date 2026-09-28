package com.pallabi.insurance.policy.controller;

import com.pallabi.insurance.policy.dto.CreatePolicyRequest;
import com.pallabi.insurance.policy.dto.PolicyResponse;
import com.pallabi.insurance.policy.model.Policy;
import com.pallabi.insurance.policy.service.PolicyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * CONTROLLER LAYER: translates HTTP <-> Java. No business logic here.
 *
 * @RestController = @Controller + @ResponseBody:
 *   every method's return value is converted to JSON and sent back.
 *
 * @RequestMapping("/api/policies") = every URL in this class starts with /api/policies.
 */
@RestController
@RequestMapping("/api/policies")
public class PolicyController {

    private final PolicyService policyService;

    // Constructor injection again: Spring passes in the PolicyService bean.
    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    /**
     * POST /api/policies  -> create a policy
     *
     * @RequestBody = read the JSON body and convert it into a CreatePolicyRequest.
     * @Valid = run the validation rules on it first. If any fail, Spring returns
     *          HTTP 400 Bad Request and this method never runs.
     *
     * Returns 201 CREATED (not 200) because a new resource was created,
     * plus a "Location" header telling the client where to find it. REST best practice.
     */
    @PostMapping
    public ResponseEntity<PolicyResponse> createPolicy(@Valid @RequestBody CreatePolicyRequest request) {
        Policy policy = policyService.createPolicy(
                request.customerName(),
                request.customerEmail(),
                request.productType(),
                request.coverageAmount(),
                request.startDate()
        );
        URI location = URI.create("/api/policies/" + policy.getPolicyNumber());
        return ResponseEntity.created(location).body(PolicyResponse.from(policy));
    }

    /**
     * GET /api/policies/{policyNumber}  -> get one policy
     * @PathVariable = take the value from the URL path.
     */
    @GetMapping("/{policyNumber}")
    public PolicyResponse getPolicy(@PathVariable String policyNumber) {
        return PolicyResponse.from(policyService.getByPolicyNumber(policyNumber));
    }

    /**
     * GET /api/policies?email=someone@example.com  -> search by email
     * @RequestParam = take the value from the query string (after the ?).
     */
    @GetMapping
    public List<PolicyResponse> getPoliciesByEmail(@RequestParam String email) {
        return policyService.getByCustomerEmail(email).stream()
                .map(PolicyResponse::from)   // convert each entity to a DTO
                .toList();
    }

    /**
     * POST /api/policies/{policyNumber}/activate
     * An ACTION on a resource. POST because it changes state.
     * (Later, Billing Service will trigger this automatically after payment.)
     */
    @PostMapping("/{policyNumber}/activate")
    public PolicyResponse activatePolicy(@PathVariable String policyNumber) {
        return PolicyResponse.from(policyService.activatePolicy(policyNumber));
    }

    @PostMapping("/{policyNumber}/cancel")
    public PolicyResponse cancelPolicy(@PathVariable String policyNumber) {
        return PolicyResponse.from(policyService.cancelPolicy(policyNumber));
    }
}
