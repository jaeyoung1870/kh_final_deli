<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  Created by IntelliJ IDEA.
  User: ykm
  Date: 2022-12-15
  Time: 오전 11:39
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Docume7898nt</title>
    <script src="https://code.jquery.com/jquery-3.6.1.js"></script>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.1/dist/css/bootstrap.min.css" rel="stylesheet"
          integrity="sha384-iYQeCzEYFbKjA/T2uDLTpkwGzCiq6soy8tYaI1GyVh/UjpbCx/TYkiZhlZB6+fzT" crossorigin="anonymous">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.1/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-u1OknCvxWvY5kfmNBILK2hRnQC3Pr17a+RTT6rIHI7NnikvbZlHgTPOOmMi466C8"
            crossorigin="anonymous"></script>

    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.2.0/css/all.css" rel="stylesheet">

    <link rel="shortcut icon" type="image/x-icon" href="/resources/favicon.ico" />
    <link rel="icon" href="/resources/favicon.ico" type="image/x-icon">

    <link rel="stylesheet" href="/resources/css/customHeader/m_common.css" type="text/css">
    <link rel="stylesheet" href="/resources/css/member/order/orderHistory.css" type="text/css">

</head>

<body>
<%@ include file="/WEB-INF/views/customHeader/m_header.jsp" %>
<%@ include file="/WEB-INF/views/customHeader/m_bell.jsp" %>
<%@ include file="/WEB-INF/views/customHeader/m_cart.jsp" %>
<%@ include file="/WEB-INF/views/customHeader/m_nav.jsp" %>
<%@ include file="/WEB-INF/views/customHeader/m_top.jsp" %>

<main id = "order_history">
    <hr class="mt55">
    <div class="container">
        <c:choose>
            <c:when test="${not empty order_list}">
                <c:forEach var="order_list" items="${order_list}" varStatus="status">

<%--                    <fmt:parseDate value="${order_list.order_date}" var="date" pattern="yyyy-mm-dd"/>--%>
<%--                       <fmt:parseNumber var="parseDate" value="${date.time+(1000*60*60*24*30)}" integerOnly="true"/>--%>

<%--                    <script>--%>
<%--                        ${parseDate}--%>
<%--                    </script>--%>
<%--&lt;%&ndash;                        <c:if test="${order_list.order_date>parseDate}">&ndash;%&gt;--%>
<%--&lt;%&ndash;                        </c:if>&ndash;%&gt;--%>
                        <div class="box1">
                        <div class="box2">
                            <span class="head_deli">배달주문</span>
                            <span class="head_date">${order_list.formDate}</span>
                            <span class="head_status">
                            ${order_list.order_status}
<%--&lt;%&ndash;                <c:if test="${i.order_status=='order'}">미접수</c:if>&ndash;%&gt;--%>
<%--&lt;%&ndash;                <c:if test="${i.order_status=='take'}">접수</c:if>&ndash;%&gt;--%>
<%--&lt;%&ndash;                <c:if test="${i.order_status=='cooking'}">조리중</c:if>&ndash;%&gt;--%>
<%--&lt;%&ndash;                <c:if test="${i.order_status=='delivering'}">배달중</c:if>&ndash;%&gt;--%>
<%--&lt;%&ndash;                <c:if test="${i.order_status=='complete'}">배달완료</c:if>&ndash;%&gt;--%>
            </span>
                        </div>
                        <div class="box3">

                            <c:if test="${order_list.store_logo==null}">
                                <div class="image-box"><img class="image-thumbnail" src="/resources/img/store/no_storelogo.png" id="profile"></div>
                            </c:if>
                            <c:if test="${order_list.store_logo!=null}">
                                <div class="image-box"><img class="image-thumbnail" src="/resources/img/store/${order_list.store_logo }" id="profile"></div>
                            </c:if>
                            <div class="info">
                                <a href="/store/menu/${order_list.store_seq}"><span class="storename">${order_list.store_name}</span></a>

                                <p class="meinfo">

                                        ${menu_list[status.index].menu.menu_name} <%-- 메뉴명--%>


                                    <c:if test = "${menu_list[status.index].count>0}">
                                        x  ${menu_list[status.index].count}
                                    </c:if> <%-- 메뉴 0개 이상일때 --%>

                                    <c:if test = "${menu_count_list[status.index]>1}">
                                        외 ${menu_count_list[status.index]-1}건
                                    </c:if>  <%-- 또다른 메뉴가 추가로 있을떄 --%>



                                        <%--                        ${basketMenu[4].menu.menu_name} x ${basketMenu[0].count}--%>

                                        <%--                    <c:if test="${basketMenu[0].menu.menu_name!=null}">--%>
                                        <%--                        <c:forEach var="menu" items="${basketMenu}" varStatus="n">--%>
                                        <%--                            <c:if test="${n.index >0}">외 ${n.index}건</c:if>--%>
                                        <%--                        </c:forEach>--%>
                                        <%--                    </c:if>--%>

                                </p>

                                <div class="infoFooter">
                                <a href="/myPage/reviewWrite/${order_list.order_seq}"><button class="deli_btn">리뷰작성</button></a>

                                <a href="/order/detail/${order_list.order_seq}"><button class="deli_btn">주문상세</button></a>
                                <button class="deli_btn">재주문</button>

                                </div>
                                <c:if test="${order_list.order_status='배달완료'}">
                                </c:if>
                            </div>
                        </div>

                    </div>

                </c:forEach>
            </c:when>
            <c:otherwise>결제내역없음</c:otherwise>
        </c:choose>

    </div>

    <hr class="mt90">
</main>
</body>
</html>
