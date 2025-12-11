package ma.saifdine.hd.ebankingbackend.web;

import lombok.RequiredArgsConstructor;
import ma.saifdine.hd.ebankingbackend.repositories.AccountOperationRepository;
import ma.saifdine.hd.ebankingbackend.services.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DashboardRestController {

    private final DashboardService dashboardService;
    private final AccountOperationRepository accountOperationRepository;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    public Map<String, Object> getStats(){
        return dashboardService.getDashboardStats();
    }

    @GetMapping("/dashboard/operationsByType")
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    public Map<String, Long> getOperationsByType(){
        return dashboardService.getOperationsByType();
    }

    @GetMapping("/dashboard/most-active-customers")
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    public Map<String, Long> getMostActiveCustomers() {
        return dashboardService.getMostActiveCustomers();
    }

}
