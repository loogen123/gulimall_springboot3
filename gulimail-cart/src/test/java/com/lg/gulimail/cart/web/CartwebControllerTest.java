package com.lg.gulimail.cart.web;

import com.lg.common.constant.AuthServerConstant;
import com.lg.common.vo.MemberResponseVo;
import com.lg.gulimail.cart.service.CartService;
import com.lg.gulimail.cart.vo.CartItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartwebControllerTest {

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartwebController cartwebController;

    @Test
    void addToCartShouldRedirectLoginWhenNotLoggedIn() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String result = cartwebController.addToCart(1L, 2, session, new RedirectAttributesModelMap());
        assertEquals("redirect:http://auth.gulimail.com/login.html", result);
        verifyNoInteractions(cartService);
    }

    @Test
    void addToCartShouldCallServiceWhenLoggedIn() throws Exception {
        MockHttpSession session = new MockHttpSession();
        MemberResponseVo member = new MemberResponseVo();
        member.setId(11L);
        session.setAttribute(AuthServerConstant.LOGIN_USER, member);
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String result = cartwebController.addToCart(1L, 2, session, redirectAttributes);

        assertEquals("redirect:http://cart.gulimail.com/addToCartSuccess.html", result);
        verify(cartService).addToCart(1L, 2, 11L);
    }

    @Test
    void addToCartShouldSkipWhenNumInvalid() throws Exception {
        MockHttpSession session = new MockHttpSession();
        MemberResponseVo member = new MemberResponseVo();
        member.setId(11L);
        session.setAttribute(AuthServerConstant.LOGIN_USER, member);

        String result = cartwebController.addToCart(1L, 0, session, new RedirectAttributesModelMap());

        assertEquals("redirect:http://cart.gulimail.com/cartList.html", result);
        verifyNoInteractions(cartService);
    }

    @Test
    void deleteItemShouldRedirectLoginWhenNotLoggedIn() {
        MockHttpSession session = new MockHttpSession();
        String result = cartwebController.deleteItem(1L, session);
        assertEquals("redirect:http://auth.gulimail.com/login.html", result);
    }

    @Test
    void updateCountShouldThrowWhenNotLoggedIn() {
        MockHttpSession session = new MockHttpSession();
        assertThrows(IllegalStateException.class, () -> cartwebController.updateCount(1L, 2, session));
    }

    @Test
    void updateCountShouldThrowWhenNumInvalid() {
        MockHttpSession session = new MockHttpSession();
        MemberResponseVo member = new MemberResponseVo();
        member.setId(11L);
        session.setAttribute(AuthServerConstant.LOGIN_USER, member);
        assertThrows(IllegalArgumentException.class, () -> cartwebController.updateCount(1L, 0, session));
    }

    @Test
    void updateCountShouldThrowWhenSkuIdInvalid() {
        MockHttpSession session = new MockHttpSession();
        MemberResponseVo member = new MemberResponseVo();
        member.setId(11L);
        session.setAttribute(AuthServerConstant.LOGIN_USER, member);

        assertThrows(IllegalArgumentException.class, () -> cartwebController.updateCount(0L, 1, session));
    }

    @Test
    void updateCountShouldReturnCartItemWhenLoggedIn() throws Exception {
        MockHttpSession session = new MockHttpSession();
        MemberResponseVo member = new MemberResponseVo();
        member.setId(11L);
        session.setAttribute(AuthServerConstant.LOGIN_USER, member);
        CartItem cartItem = new CartItem();
        cartItem.setSkuId(1L);
        when(cartService.addToCart(1L, 2, 11L)).thenReturn(cartItem);

        CartItem result = cartwebController.updateCount(1L, 2, session);

        assertEquals(1L, result.getSkuId());
    }
}
