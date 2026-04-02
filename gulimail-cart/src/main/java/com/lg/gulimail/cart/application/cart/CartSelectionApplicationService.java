package com.lg.gulimail.cart.application.cart;

import com.lg.gulimail.cart.application.port.out.CartSelectionPort;
import com.lg.gulimail.cart.domain.cart.CartSelectionCommand;
import com.lg.gulimail.cart.domain.cart.CartSelectionDomainService;
import com.lg.gulimail.cart.domain.cart.CartSelectionResult;
import com.lg.gulimail.cart.vo.CartItem;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartSelectionApplicationService {
    private final CartSelectionPort cartSelectionPort;
    private final CartSelectionDomainService cartSelectionDomainService;

    public CartSelectionApplicationService(CartSelectionPort cartSelectionPort, CartSelectionDomainService cartSelectionDomainService) {
        this.cartSelectionPort = cartSelectionPort;
        this.cartSelectionDomainService = cartSelectionDomainService;
    }

    public List<CartItem> getCurrentUserCheckedItems() {
        return cartSelectionPort.getCurrentUserCheckedItems();
    }

    public CartSelectionResult checkItem(Long skuId, Integer check) {
        CartSelectionCommand command = cartSelectionDomainService.normalize(CartSelectionCommand.of(skuId, check));
        CartSelectionResult validate = cartSelectionDomainService.validate(command);
        if (!validate.isSuccess()) {
            return validate;
        }
        cartSelectionPort.checkItem(command.getSkuId(), command.getCheck());
        return CartSelectionResult.success();
    }
}
