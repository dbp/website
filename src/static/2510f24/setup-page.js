$(function(){
  $("div.PythonHighlight, span.PythonHighlight, pre.PythonHighlight").each(function(_,code) {
    CodeMirror.runMode($(code).text(), "python", code);
    $(code).addClass("cm-s-default");
  });
  /*
  $("pre.PyretHighlight, span.PyretHighlight").each(function(_,code) {
    CodeMirror.runMode($(code).text(), "pyret", code);
    $(code).addClass("cm-s-default");
  });
  $("div.JavaHighlightBlock").each(function(_,code) {
    var lines = $(code).find("td").map(function(_,td) { return $(td).text(); }).toArray().join("\n");
    CodeMirror.runMode(lines, "java", code);
    $(code).addClass("cm-s-default").addClass("RktBlk");
  });
  $("span.JavaHighlight").each(function(_,code) {
    CodeMirror.runMode($(code).text(), "java", code);
    $(code).addClass("cm-s-default").addClass("RktBlk");
  });
  */
  var tocset = document.querySelector("div.tocset");
  if (!tocset) { return ; } // Dunno why this is needed, but it seems to help
  tocset = tocset.outerHTML.replace(new RegExp("tocview_\\d+", "g"), "topbar_$&");
  tocset = $(tocset);  
  var container = $("<div>").addClass("topbar navleft").addClass("hidden");
  var btn = $("<a>").attr("href", "javascript:void(0);").text("Table of Contents");
  btn.click(function() { container.toggleClass("hidden"); });
  container.append(btn).append(tocset);
  $("div.navsettop").prepend(container);
});

