function insertHtmlAtCursor(html) {
    console.log(html);
    var sel, range, node;
    if (window.getSelection) {
        sel = window.getSelection();
        if (sel.getRangeAt && sel.rangeCount) {
            range = window.getSelection().getRangeAt(0);
            node = range.createContextualFragment(html);
            range.insertNode(node);
        }
    } else if (document.selection && document.selection.createRange) {
        document.selection.createRange().pasteHTML(html);
    }
}

function isCursorInATable() {
    var selection = window.getSelection();
    var range = selection.getRangeAt(0);
    var node = range.startContainer;
    if (node.parentNode.nodeName=="TD") {
        return true;
    }
    return false;
}

/**
 * insert a new ligne to current table after current line
 */
function insertLine() {
    if (isCursorInATable()) {
        var selection = window.getSelection();
        var range = selection.getRangeAt(0);
        var node = range.startContainer;
        var currentTR;
        var tbody;
        if (node.parentNode.nodeName==="TD") {
            var current = node.parentNode;
            while (current.nodeName!=="TR") {
                current=current.parentNode;
            }
            currentTR=current;
            var nTD = 0;
            for (var i=0;i<currentTR.childNodes.length;i++) {
                var n = currentTR.childNodes.item(i);
                if (n.nodeName==="TD") nTD++;
            }
            current=currentTR.parentNode;
            while (current.nodeName!="TBODY") {
                current=current.parentNode;
            }
            tbody=current;
            var newTR=document.createElement("TR");
            for (i=0;i<nTD;i++) {
                var newTD = document.createElement("TD");
                newTD.textContent=String.fromCharCode(160);
                newTR.appendChild(newTD);
            }
            tbody.insertBefore(newTR,currentTR.nextSibling);
        }
    }
}


function removeLine() {
    if (isCursorInATable()) {
        var selection = window.getSelection();
        var range = selection.getRangeAt(0);
        var node = range.startContainer;
        var currentTR;
        var tbody;
        if (node.parentNode.nodeName==="TD") {
            var current = node.parentNode;
            while (current.nodeName!=="TR") {
                current=current.parentNode;
            }
            currentTR=current;

            current=currentTR.parentNode;
            while (current.nodeName!="TBODY") {
                current=current.parentNode;
            }
            tbody=current;
            tbody.removeChild(currentTR);
        }
    }
}

function insertCol() {
    if (isCursorInATable()) {
        var selection = window.getSelection();
        var range = selection.getRangeAt(0);
        var node = range.startContainer;
        var currentTD;
        var nodeOffset=0;
        var tbody;
        if (node.parentNode.nodeName==="TD") {
            currentTD = node.parentNode;
            tbody=currentTD.parentNode.parentNode;

            // compute current TD offset
            var n = currentTD;
            while (n.previousSibling!=undefined) {
                nodeOffset++;
                n= n.previousSibling;
            }

            // add new td at specified offset
            var tbodyChilds = tbody.childNodes;
            for (var i=0;i<tbodyChilds.length;i++) {
                var line = tbodyChilds.item(i);
                if (line.nodeName!=="TR") continue;
                var lineChilds = line.childNodes;
                var currentOffset = 0 ;
                var tdBefore =null;
                for (var j=0;j<lineChilds.length;j++) {
                    var td = lineChilds.item(j);
                    if (td.nodeName==="TD") {
                        if (currentOffset==(nodeOffset+1)) {
                            tdBefore = td;
                            break;
                        }
                        currentOffset++;
                    }
                }
                var newTD = document.createElement("TD");
                newTD.textContent=String.fromCharCode(160);
                line.insertBefore(newTD,tdBefore);
            }
        }
    }
}
function focus() {
    document.body.focus();
}

function removeCol() {
    if (isCursorInATable()) {
        var selection = window.getSelection();
        var range = selection.getRangeAt(0);
        var node = range.startContainer;
        var currentTD;
        var nodeOffset=0;
        var tbody;
        if (node.parentNode.nodeName==="TD") {
            currentTD = node.parentNode;
            tbody=currentTD.parentNode.parentNode;

            // compute current TD offset
            var n = currentTD;
            while (n.previousSibling!=undefined) {
                nodeOffset++;
                n= n.previousSibling;
            }

            // remove td at specified offset
            var tbodyChilds = tbody.childNodes;
            for (var i=0;i<tbodyChilds.length;i++) {
                var line = tbodyChilds.item(i);
                if (line.nodeName!=="TR") continue;
                var lineChilds = line.childNodes;
                var currentOffset = 0 ;
                var tdBefore =null;
                for (var j=0;j<lineChilds.length;j++) {
                    var td = lineChilds.item(j);
                    if (td.nodeName==="TD") {
                        if (currentOffset==nodeOffset) {
                            line.removeChild(td);
                            break;
                        }
                        currentOffset++;
                    }
                }
            }
        }
    }
}