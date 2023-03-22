package jpabook.jpashop.domain.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.Item.Book;
import jpabook.jpashop.Item.Item;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.service.OrderService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired EntityManager em;
    @Autowired
    OrderService orderService;
    @Autowired OrderRepository orderRepository;

    @Test
    public void 상품주문() {
        //given
        Member member = createMember();

        Book book = createBook("시골 JPA", 10000, 10);

        //when
        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order getOrder = orderRepository.findOne(orderId);

        //상품 주문시 상태는 ORDER
        assertThat(getOrder.getStatus()).isEqualTo(OrderStatus.ORDER);
        //주문한 상품 종류 수가 정확해야 한다.
        assertThat(getOrder.getOrderItems().size()).isEqualTo(1);
        //주문 가격은 가격*수량 이다.
        assertThat(getOrder.getTotalPrice()).isEqualTo(10000*orderCount);
        //주문 수량만큼 재고가 줄어야 한다.
        assertThat(book.getStockQuantity()).isEqualTo(8);

    }



    @Test
    public void 상품주문_재고수량초과() {
        //when
        Member member = createMember();
        Item item = createBook("시골 JPA", 10000, 10);

        //given
        int orderCount = 11;

        //then
        Assertions.assertThatThrownBy(() -> orderService.order(member.getId(), item.getId(), orderCount))
                .isInstanceOf(NotEnoughStockException.class);

    }

    @Test
    public void 주문취소() {
        //given
        Member member = createMember();
        Book item = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);

        //when
        orderService.cancelOrder(orderId);

        //then
        Order getOrder = orderRepository.findOne(orderId);

        //주문 취소시 상태는 CANCEL 이다.
        assertThat(getOrder.getStatus()).isEqualTo(OrderStatus.CANCEL);
        //주문이 취소된 상품은 재고가 증가해야 한다.
        assertThat(item.getStockQuantity()).isEqualTo(10);

    }



    private Book createBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "강가", "123-123"));
        em.persist(member);
        return member;
    }

}