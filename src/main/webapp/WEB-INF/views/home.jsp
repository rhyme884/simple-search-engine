<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<html>

<head>
    <meta charset="utf-8" />
</head>
<style>
    @font-face {
        font-family: 'overwatch';
        src: url('fonts/koverwatch.woff2');
    }
    body {
    	text-align: center;
    }
    .parent {
    	width: 1000px;
    	height: 20px;
    	text-align: center;
    	margin-left: 250px;
    }
    .category {
    	display: inline-block; 
    	text-align: left;
    }
</style>

<body>
    <script src="https://d3js.org/d3.v3.min.js"></script>
    <script src="https://rawgit.com/jasondavies/d3-cloud/master/build/d3.layout.cloud.js" type="text/JavaScript"></script>
    <script>
        var width = 600,
            height = 540

        var svg = d3.select("body").append("svg")
            .attr("width", width)
            .attr("height", height)
        	.attr("align", "center");
        d3.csv("/data/worddata.csv", function (data) {
            showCloud(data)
            setInterval(function(){
                 showCloud(data)
            },2000) 
        });
        //scale.linear: 선형적인 스케일로 표준화를 시킨다. 
        //domain: 데이터의 범위, 입력 크기
        //range: 표시할 범위, 출력 크기 
        //clamp: domain의 범위를 넘어간 값에 대하여 domain의 최대값으로 고정시킨다.
        wordScale = d3.scale.linear().domain([0, 1700]).range([0, 70]).clamp(true);
        var svg = d3.select("svg")
                    .append("g")
                    .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")")

        function showCloud(data) {
            d3.layout.cloud().size([width, height])
                //클라우드 레이아웃에 데이터 전달
                .words(data)
                .rotate(function (d) {
                    return d.text.length >= 3 ? 90 : 0;
                })
                //스케일로 각 단어의 크기를 설정
                .fontSize(function (d) {
                    return wordScale(d.frequency);
                })
                //클라우드 레이아웃을 초기화 > end이벤트 발생 > 연결된 함수 작동  
                .on("end", draw)
                .start();

            function draw(words) { 
                var cloud = svg.selectAll("text").data(words)
                var palette = ["green", "#fbc280", "skyblue", "#db7093", "#405275", "#8b4513", "#d2691e",
                    "#6b8e23", "#b22222", "#48d1cc"];
                var idx = 0;
                //Entering words
                cloud.enter()
                    .append("text")
                    .style("font-family", "Impact")
                    .style("fill", function (d) {
                        if (idx >= 10) idx =0;
                        return palette[idx++];
                    })
                    .style("fill-opacity", .5)
                    .attr("text-anchor", "middle") 
                    .attr('font-size', 1)
                    .text(function (d) {
                        return d.text;
                    }); 
                cloud
                    .transition()
                    .duration(600)
                    .style("font-size", function (d) {
                        return d.size + "px";
                    })
                    .attr("transform", function (d) {
                        return "translate(" + [d.x, d.y] + ")rotate(" + d.rotate + ")";
                    })
                    .style("fill-opacity", 1)
                    .each(function() {
                        d3.select(this).on("click", function(d) {
                            document.getElementById('search').value = d.text;
                            document.getElementById('searchFrm').submit();
                            }
                        )});
            }
        }
    </script>
    <form action="/query.do" method="POST" id="searchFrm">
    	<input type="text" name="query" id="search" style="width:300px;height:30px;fontSize:14px" />
    	<input type="submit" value="검색" />
    </form>
    <p>☎  rhyme884@gmail.com / github: rhyme884</p>
   	<div class="parent">
   		<div id= "politics" class="ctg1 category">
    		<form action="/category/0" method="POST">
    			<input type="submit" value="정치" />
    		</form>
    	</div>
    	<div id ="economy" class="ctg2 category">
    		<form action="/category/1" method="POST">
    			<input type="submit" value="경제" />
    		</form>
    	</div>
    	<div id="culture" class="ctg3 category">
    		<form action="/category/2" method="POST">
    			<input type="submit" value="문화" />
    		</form>
   		</div>
   		<div id= "global" class="ctg4 category">
    		<form action="/category/3" method="POST">
    			<input type="submit" value="글로벌" />
    		</form>
    	</div>
    	<div id= "science" class="ctg5 category">
    		<form action="/category/4" method="POST">
    			<input type="submit" value="IT/과학" />
    		</form>
    	</div>
    	<div id= "society" class="ctg6 category">
    		<form action="/category/5" method="POST">
    			<input type="submit" value="사회" />
    		</form>
    	</div>
    </div>
</body>

</html>
