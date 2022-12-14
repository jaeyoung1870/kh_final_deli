package kh.deli.domain.member.order.mapper;

import kh.deli.global.entity.OrdersDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface OrderOrdersMapper {

    public void insert(@Param("orders")OrdersDTO ordersDTO);


}