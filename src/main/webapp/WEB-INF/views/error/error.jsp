<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>

<style>
@import
	url(http://fonts.googleapis.com/css?family=Bree+Serif|Source+Sans+Pro:300,400)
	;

* {
	maring: 0;
	padding: 0;
}

body {
	font-family: 'Source Sans Pro', sans-serif;
	background: #436e90;
	color: #1f3759;
}

a:link {
	color: #1f3759;
	text-decoration: none;
}

a:active {
	color: #1f3759;
	text-decoration: none;
}

a:hover {
	color: #9fb7d9;
	text-decoration: none;
}

a:visited {
	color: #1f3759;
	text-decoration: none;
}

a.underline, .underline {
	text-decoration: underline;
}

.bree-font {
	font-family: 'Bree Serif', serif;
}

#content {
	margin: 0 auto;
	width: 960px;
}

.clearfix:after {
	content: ".";
	display: block;
	clear: both;
	visibility: hidden;
	line-height: 0;
	height: 0;
}

.clearfix {
	display: block;
}

#logo {
	margin: 1em;
	float: left;
	display: bloack;
}

nav {
	float: right;
	display: block;
}

nav ul>li {
	list-style: none;
	float: left;
	margin: 0 2em;
	display: block;
}

#main-body {
	text-align: center;
}

.enormous-font {
	font-size: 10em;
	margin-bottom: 0em;
}

.big-font {
	font-size: 1.4em;
}

hr {
	width: 25%;
	height: 1px;
	background: #1f3759;
	border: 0px;
}
</style>

</head>
<body>
	<div id="content">

		<div class="clearfix"></div>

		<div id="main-body">
			<p class="enormous-font bree-font">${errorCode}</p>
			<p class="big-font">죄송합니다. 요청하신 페이지는 존재하지 않습니다.</p>
			<hr>
			<p class="big-font">
				<a href="home"><font class="underline">홈</font>으로 돌아가기</a>
			</p>
		</div>
	</div>
</body>

</html>