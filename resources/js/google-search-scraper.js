/**
 * Created by kipu5728 on 10/27/16.
 */

var page = require('webpage').create();
page.settings.userAgent =
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.71 Safari/537.36";

page.onConsoleMessage = function (msg) {
    console.log(msg);
}

var query = concatQuery();
var url = "https://www.google.com/search?q="+query.replace(new RegExp(' ', 'g'),'+');

main();

function main(){
    sleep(500);
    spell(query);
}

function concatQuery(){
    var system = require('system');

    var query = "";
    system.args.forEach(function (arg, i) {
        if(i !== 0){
            query += arg + " ";
        }
    });

    return query;
}

function sleep(milliseconds) {
    var start = new Date().getTime();
    while(true){
        if ((new Date().getTime() - start) > milliseconds){
            break;
        }
    }
}
function spell(query){
    page.open(url, function (status) {
        if(status === "success"){
            var spelling = page.evaluate(function () {
                var spellInfos = document.getElementsByClassName("spell");

                if(spellInfos.length > 0){
                    var spellTag = spellInfos[0].textContent;
                    var spelling = spellInfos[1].textContent;

                    if(spellTag.length === 0 && spelling.length === 0){
                        var searchList = document.getElementsByClassName("srg")[0];

                        if(searchList === undefined){
                            return "";
                        }

                        if(searchList.length === 0){
                            return "EXCEEDED";
                        }

                        return "";
                    }

                    return spellTag.toLowerCase() +" | "+spelling;
                }
                else{
                    return "EXCEEDED";
                }
            });

            if(spelling.length === 0){
                spelling = "none | "+query
            }

            if(spelling === "EXCEEDED"){
                sleep(60000);
                spell(query);
            }
            else{
                console.log(spelling);
                phantom.exit();
            }
        }
        else{
            // console.log(status);
            sleep(60000);
            spell(query);
        }
    });


}
