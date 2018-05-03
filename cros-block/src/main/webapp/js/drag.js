//DayLv -122821315@qq.com

function allowDrop(ev){
	ev.preventDefault();
	}

function drag(ev){
	ev.dataTransfer.setData("Text",ev.target.id);
}

function drop(ev){
	ev.preventDefault();
	var data=ev.dataTransfer.getData("Text");
	var item = document.getElementById(data).cloneNode();
	item.innerHTML = document.getElementById(data).innerHTML
    ev.target.appendChild(item);
}
