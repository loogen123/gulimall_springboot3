$(function () {
    // 1. 路径改为接口地址，确保触发 Controller
    $.getJSON("/index/catalog.json", function (data) {
        console.log("Ajax请求成功，开始渲染！");
        var ctgall = data;

        // 2. 注意：选择器改回 li 标签，因为 ctg-data 在 li 上
        $(".header_li2").each(function () {
            var $thisLi = $(this);
            var ctg1Id = $thisLi.attr("ctg-data"); // 获取一级分类ID

            if (ctg1Id && ctgall[ctg1Id]) {
                var ctg2list = ctgall[ctg1Id];
                var panel = $("<div class='header_main_left_main'></div>");
                var panelol = $("<ol class='header_ol'></ol>");

                $.each(ctg2list, function (i, ctg2) {
                    // 二级分类标题
                    var cata2link = $("<a href='#' style='color: #111; font-weight: bold;' class='aaa'>" + ctg2.name + " ></a>");
                    var li = $("<li></li>");

                    // 三级分类列表
                    var ctg3List = ctg2["catalog3List"];
                    var len = 0;
                    if (ctg3List) {
                        $.each(ctg3List, function (j, ctg3) {
                            var cata3link = $("<a href='http://search.gulimail.com/list.html?catalog3Id=" + ctg3.id + "' style='color: #999; padding: 0 5px;'>" + ctg3.name + "</a>");
                            li.append(cata3link);
                            len = len + 1 + ctg3.name.length;
                        });
                    }

                    // 动态高度处理
                    if (len >= 46 && len < 92) {
                        li.attr("style", "height: 60px;");
                    } else if (len >= 92) {
                        li.attr("style", "height: 90px;");
                    }

                    panelol.append(cata2link).append(li);
                });

                panel.append(panelol);
                // 3. 关键：把生成的面板插在 li 里的 a 标签后面
                $thisLi.find(".header_main_left_a").after(panel);
            }
        });
    });
});