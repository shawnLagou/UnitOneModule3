<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page isELIgnored="false" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>数据查询列表</title>
</head>
<script type="text/javascript" src="/js/jquery.min.js"></script>
    <script type="text/javascript">

    function edit() {
        var a = document.getElementsByName("cb");
        var  table = document.getElementById("dlg");
        var num = 0;
        for(var i=0;i<a.length;i++)
        {
            if(a[i].checked && num < 1){
                num += 1;
                var row = a[i].parentElement.parentElement.rowIndex;
                var id = table.rows[row].cells[1].getElementsByTagName("input")[0].value;
                var name = (table.rows[row].cells[2].getElementsByTagName("input")[0].value);
                var phone = (table.rows[row].cells[3].getElementsByTagName("input")[0].value);
                var address = (table.rows[row].cells[4].getElementsByTagName("input")[0].value);
                var data = {"id": id, "name": name, "phone": phone, "address": address};
                $.ajax({
                    type: "POST",//方法类型
                    dataType: "json",//预期服务器返回的数据类型
                    url: "/jpa/save",//url
                    contentType: "application/json; charset=utf-8",
                    data: JSON.stringify(data),
                    success: function (result) {
                        console.log(result);//打印服务端返回的数据
                            alert("系统提示： 编辑成功");
                            window.location.reload();
                   }
                });
            }
        }
        if (num != 1) {
            alert("请选择一条数据");
        }
    }


    function deleteResume() {
        var a = document.getElementsByName("cb");
        var  table = document.getElementById("dlg");
        var num = 0;
        for(var i=0;i<a.length;i++) {
            if(a[i].checked && num < 1){
                num += 1;
                var row = a[i].parentElement.parentElement.rowIndex;
                var id = table.rows[row].cells[1].getElementsByTagName("input")[0].value;
                var name = (table.rows[row].cells[2].getElementsByTagName("input")[0].value);
                var phone = (table.rows[row].cells[3].getElementsByTagName("input")[0].value);
                var address = (table.rows[row].cells[4].getElementsByTagName("input")[0].value);
                var data = {"id": id, "name": name, "phone": phone, "address": address};
                $.ajax({
                    type: "DELETE",//方法类型
                    dataType: "json",//预期服务器返回的数据类型
                    url: "/jpa/delete",//url
                    contentType: "application/json; charset=utf-8",
                    data: JSON.stringify(data),
                    success: function (result) {
                        console.log(result);//打印服务端返回的数据
                        alert("系统提示：删除成功");
                        window.location.reload();
                    }
                });
            }
        }
        if (num != 1) {
            alert("请选择一条数据");
        }

    }

    function addtr(){
        var trHTML = "<tr>\n" +
            "                <td><input name = 'cb' type=\"checkbox\"></td>\n" +
            "                <td ><input id = \"id\" type=\"text\" value=> </td>\n" +
            "                <td ><input id = \"name\" type=\"text\" value= ></td>\n" +
            "                <td ><input id = \"phone\" type=\"text\" value=> </td>\n" +
            "                <td ><input id = \"address\" type=\"text\" value=> </td>\n" +
            "                <td><input type=\"button\" onclick=\"edit()\" value=\"edit\"></td>\n" +
            "                <td><input type=\"button\" onclick=\"deleteResume()\" value=\"delete\"></td>\n" +
            "            </tr>";
        $("#dlg").append(trHTML);
    }

</script>



<body>

<table id= "dlg" action="/jpa/query" method="GET" rownumber = "true" fitColumns="true" >

        <tr>
            <td>    </td>
            <td>编号</td>
            <td>姓名</td>
            <td>电话</td>
            <td>地址</td>
            <td><input type="button" onclick="addtr()" value="add"></td>


        </tr>
        <c:forEach items="${datasList}" var="item">
            <tr>
                <td><input name = 'cb' type="checkbox"></td>
                <td ><input id = "id" type="text" value=${item.id}> </td>
                <td ><input id = "name" type="text" value=${item.name} ></td>
                <td ><input id = "phone" type="text" value=${item.phone}> </td>
                <td ><input id = "address" type="text" value=${item.address}> </td>
                <td><input type="button" onclick="edit()" value="save"></td>
                <td><input type="button" onclick="deleteResume()" value="delete"></td>
            </tr>
        </c:forEach>
</table>
</body>

</html>