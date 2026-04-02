package com.lg.gulimail.search.application.search;

import com.lg.common.to.es.SkuEsModel;
import com.lg.gulimail.search.application.port.out.ProductUpPort;
import com.lg.gulimail.search.application.port.out.SearchQueryPort;
import com.lg.gulimail.search.domain.search.ProductUpDomainService;
import com.lg.gulimail.search.domain.search.ProductUpResult;
import com.lg.gulimail.search.domain.search.SearchDomainService;
import com.lg.gulimail.search.domain.search.SearchQueryCommand;
import com.lg.gulimail.search.domain.search.SearchQueryResult;
import com.lg.gulimail.search.vo.SearchParam;
import com.lg.gulimail.search.vo.SearchResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchApplicationService {
    private final SearchQueryPort searchQueryPort;
    private final ProductUpPort productUpPort;
    private final SearchDomainService searchDomainService;
    private final ProductUpDomainService productUpDomainService;

    public SearchApplicationService(SearchQueryPort searchQueryPort,
                                    ProductUpPort productUpPort,
                                    SearchDomainService searchDomainService,
                                    ProductUpDomainService productUpDomainService) {
        this.searchQueryPort = searchQueryPort;
        this.productUpPort = productUpPort;
        this.searchDomainService = searchDomainService;
        this.productUpDomainService = productUpDomainService;
    }

    public SearchQueryResult search(SearchParam param, String queryString) {
        SearchQueryCommand command = searchDomainService.normalize(param, queryString);
        SearchQueryResult validateResult = searchDomainService.validate(command);
        if (!validateResult.isSuccess()) {
            return validateResult;
        }
        SearchResult result = searchQueryPort.search(command.getSearchParam());
        return searchDomainService.success(result);
    }

    public ProductUpResult productStatusUp(List<SkuEsModel> skuEsModels) {
        ProductUpResult validateResult = productUpDomainService.validate(skuEsModels);
        if (!validateResult.isSuccess()) {
            return validateResult;
        }
        try {
            boolean hasFailures = productUpPort.productStatusUp(skuEsModels);
            return hasFailures ? ProductUpResult.failed() : ProductUpResult.success();
        } catch (Exception e) {
            return ProductUpResult.failed();
        }
    }
}
