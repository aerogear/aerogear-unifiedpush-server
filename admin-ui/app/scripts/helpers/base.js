var konami_keys = [38, 38, 40, 40, 37, 39, 37, 39, 66, 65];
var konami_index = 0;
$(document).keydown(function kk(e){
    if(e.keyCode === konami_keys[konami_index++]){
        if(konami_index === konami_keys.length){
            $(document).unbind('keydown', kk);
            console.log( "AWESOMENESS" );
        }
    }else{
        konami_index = 0;
    }
});
