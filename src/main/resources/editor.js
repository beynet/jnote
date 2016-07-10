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


function getParentTD() {
    var selection = window.getSelection();
    var range = selection.getRangeAt(0);
    var node = range.startContainer;
    while (node.parentNode!=null) {
        node=node.parentNode;
        if (node.nodeName == "TD") {
            return node;
        }
    }
    return null;
}

function isCursorInATable() {
    var node = getParentTD();
    if (node!=null) return true;
    return false;
}

/**
 * insert a new ligne to current table after current line
 */
function insertLine() {
    if (isCursorInATable()) {
        var currentTR;
        var tbody;
        var td = getParentTD();
        if (td!=null) {
            var current = td.parentNode;
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
        var td = getParentTD();
        var currentTR;
        var tbody;
        if (td!=null) {
            var current = td.parentNode;
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
        var currentTD = getParentTD();
        var nodeOffset=0;
        var tbody;
        if (currentTD!=null) {
            var current = currentTD;
            while (current.nodeName!="TBODY") {
                current=current.parentNode;
            }
            tbody=current;

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
        var currentTD=getParentTD();
        var nodeOffset=0;
        var tbody;
        if (currentTD!=null) {
            var current = currentTD;
            while (current.nodeName!="TBODY") {
                current=current.parentNode;
            }
            tbody=current;

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