package ma.saifdine.hd.ebankingbackend.services;

import java.util.Map;

public interface DashboardService {

    Map<String, Object> getDashboardStats();

    Map<String, Long> getOperationsByType();

    Map<String, Long> getMostActiveCustomers();

}
