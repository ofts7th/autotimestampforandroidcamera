var currentVer = 0;
var baseUrl = "";
var machineId = "";
var isDebugMode = false;
var csInited = false;

//Global function
function csWait(cfg) {
    cfg.times = 0;
    var id = setInterval(function() {
        cfg.times++;
        if (cfg.times > 30) {
            clearInterval(id);
        }
        var valid = cfg.when();
        if (valid) {
            clearInterval(id);
            cfg.work();
        }
    }, 100);
}
function isNullOrEmpty(s) {
    if (!s)
        return true;
    if (s == null)
        return true;
    if (s == "")
        return true;
    return false;
}
function setLocation(url) {
    window.location.href = url;
}
function gotoWebPage(url) {
    $.cs.gotoPage({ source: 'web', url: url });
}
function goback() {
    $.cs.goBack();
}
function showMessage(msg) {
    $.cs.hideLoading();
    $.cs.showMessage(msg);
}
function getConfig(k, v) {
    var o = local.getConfig(k);
    if(o == ''){
        if(v){
            return v;
        }
    }
    return o;
}
function saveConfig(k, v) {
    local.saveConfig(k, v);
}
function execUploadCompleteCallback(ret){
	$.cs.endProgress();
    $.cs.cbUploadComplete($.cs.parseJson(ret));
}
function execDownloadCompleteCallback(ret){
	$.cs.endProgress();
	$.cs.cbDownloadComplete(ret);
}
function cbProgressError(m){
	$.cs.endProgress();
	$.cs.showMessage(m);
}

var localCallback = [];
function execNetCallback(k) {
    $.cs.hideLoading();
    if (localCallback[k]) {
        var ret = local.getWebResult(k);
        var isJson = false;
		if(typeof(ret) == 'string'){
			ret = $.trim(ret);
			if (ret.charAt(0) == '{' || ret.charAt(0) == '[') {
				isJson = true;
				ret = eval('(' + ret + ')');
			}
		}
		if(typeof(ret) == 'object'){
			isJson = true;
		}
		if(isJson){
			if(ret.exceptioncode){
				if(ret.exceptioncode == 1){
					gotoWebPage(ret.loginUrl);
					return; 
				}
			}
		}
        localCallback[k](ret);
    }
}
function execProgressCallback(p) {
    $.cs.cbProgress(p);
}
//Cs object
$.cs = {};
$.extend($.cs, {
	parseJson: function(str){
		return eval('(' + str + ')');
	},
    stringify: function(obj) {
        if (window.JSON) {
            return JSON.stringify(obj);
        }
    },
    newGuid: function() {
        var g = "";
        var i = 32;
        while (i--) {
            g += Math.floor(Math.random() * 16.0).toString(16);
        }
        return g;
    },

    loading: false,
    showLoading: function() {
        $.cs.loading = true;
        var win = $(window);
        var loadingWindows = $('#global-loading-window');
        loadingWindows.show();
        loadingWindows.css({
            'left': ((win.width() - 60) / 2) + 'px',
            'top': ((win.height() - 60) / 2) + 'px'
        });
    },
    hideLoading: function() {
        if ($.cs.loading) {
            $('#global-loading-window').hide();
            $.cs.loading = false;
        }
    },
    ajaxGet: function(o) {
        $.cs.showLoading();
        if (o.success) {
            var guid = $.cs.newGuid();
            localCallback[guid] = o.success;
            local.getWebData(guid, $.cs.formatUrl(o.url));
        } else {
            local.getWebData('', $.cs.formatUrl(o.url));
        }
    },
    ajaxPost: function(o) {
        $.cs.showLoading();
        if (o.success) {
            var guid = $.cs.newGuid();
            localCallback[guid] = o.success;
            local.postWebData(guid, $.cs.formatUrl(o.url), o.data);
        } else {
            local.postWebData('', $.cs.formatUrl(o.url), o.data);
        }
    },
    ajaxPostJson: function(o) {
        o.data = $.cs.stringify(o.data);
        $.cs.ajaxPost(o);
    },
    
    composeUrl: function(url, k, v) {
        if (url.indexOf('?') == -1) {
            return url + '?' + k + '=' + v;
        } else {
            var arr_url = url.split('?');
            var para = arr_url[1];
            var arr_para = para.split('&');
            var replaced = false;
            for (var i = 0; i < arr_para.length; i++) {
                var arr_kv = arr_para[i].split('=');
                if (arr_kv[0] == k) {
                    arr_para[i] = k + '=' + v;
                    replaced = true;
                    break;
                }
            }
            if (replaced) {
                url = arr_url[0] + '?';
                for (var i = 0; i < arr_para.length; i++) {
                    if (i > 0) {
                        url += '&';
                    }
                    url += arr_para[i];
                }
                return url;
            } else {
                return url + '&' + k + '=' + v;
            }
        }
    },
    formatUrl: function(url) {
        if (url.length > 4 && url.substr(0, 4) == 'http')
            return url;

        if (url.charAt(0) != '/') {
            url = '/' + url;
        }
        url = baseUrl + url;
        url = $.cs.composeUrl(url, "mid", machineId);
        url = $.cs.composeUrl(url, "v", (new Date()).getTime());
        return url;
    },

    showMessage: function(msg) {
        var globalBtnMessageOk = $('#globalBtnMessageOk');
        if(globalBtnMessageOk.is(':visible')){
        	$.cs.closePopup();
        }
        globalBtnMessageOk.unbind('click');
        globalBtnMessageOk.click(function() {
            $.cs.closePopup();
        });
        $('#divGlobalAlert .message').html(msg);
        $.cs.showPopup({
            content: $('#divGlobalAlert')
        });
        $.cs.setPopupTitle('提示');
    },
    showMessageBox: function(o) {
        var globalBtnMessageOk = $('#globalBtnMessageOk');
        if(globalBtnMessageOk.is(':visible')){
        	$.cs.closePopup();
        }
        globalBtnMessageOk.unbind('click');
        globalBtnMessageOk.click(function() {
            $.cs.closePopup();
            o.cbOk();
        });
        $('#divGlobalAlert .message').html(o.message);
        $.cs.showPopup({
            content: $('#divGlobalAlert')
        });
        $.cs.setPopupTitle('提示');
    },
    showConfirm: function(o) {
        var globalBtnConfirmYes = $('#globalBtnConfirmYes');
        globalBtnConfirmYes.unbind('click');
        globalBtnConfirmYes.click(function() {
            $.cs.closePopup();
            o.cbYes();
        });
        var globalBtnConfirmNo = $('#globalBtnConfirmNo');
        globalBtnConfirmNo.unbind('click');
        globalBtnConfirmNo.click(function() {
            $.cs.closePopup();
            if(o.cbNo){
                o.cbNo();
            }
        });
        if (o.labeYes) {
            globalBtnConfirmYes.html(o.labeYes);
        } else {
            globalBtnConfirmYes.html('是');
        }
        if (o.labeNo) {
            globalBtnConfirmNo.html(o.labeNo);
        } else {
            globalBtnConfirmNo.html('否');
        }
        $('#divGlobalConfirm .message').html(o.message);
        $.cs.showPopup({
            content: $('#divGlobalConfirm')
        });
        $.cs.setPopupTitle('提示');
    },

    popupStatck: new Array(),
    showPopup: function(opt) {
        var win = $(window);
        var popUpBg = $('#global-cs-popup-background');
        if (popUpBg.is(':hidden')) {
            popUpBg.height(win.height() + 'px');
            popUpBg.width(win.width() + 'px');
            popUpBg.show();
        }else{
        	popUpBg.css('z-index', parseInt(popUpBg.css('z-index')) + 2);
        }

        var popUpWin = $('<div class="cs_popup"><div class="title_bar"><span class="title"></span></div><div class="content"></div></div>');
        if (opt.url) {
        } else {
            popUpWin.contenOwner = opt.content.parent();
            if(!opt.content.attr('id')){
            	opt.content.attr('id', $.cs.newGuid());
            }
            popUpWin.contentId = opt.content.attr('id');
            popUpWin.find('.content').append(opt.content);

            $('body').append(popUpWin);
            popUpWin.width((win.width() - 40) + 'px');
            popUpWin.css({
                'z-index': parseInt(popUpBg.css('z-index')) + 1,
                'top': ((win.height() - popUpWin.height()) / 2) + 'px',
                'left': '20px'
            });
            $.cs.popupStatck.push({
                win: popUpWin,
                callBack: opt.callBack
            });
            var divHeader = popUpWin.find('.popupheader');
			if(divHeader.length > 0){
				$.cs.setPopupTitle(divHeader.html());
			}
        }
    },
    setPopupTitle: function(title) {
        if ($.cs.popupStatck.length > 0) {
            $.cs.popupStatck[$.cs.popupStatck.length - 1].win.find('.title_bar .title').html(title);
        }
    },
    closePopup: function(ret) {
        if ($.cs.popupStatck.length == 0)
            return;

        var p = $.cs.popupStatck.pop();
        if (p.win.contenOwner != undefined) {
            p.win.contenOwner.append(p.win.find('#' + p.win.contentId));
        }
        p.win.remove();
        if ($.cs.popupStatck.length == 0) {
            $('#global-cs-popup-background').hide();
        } else {
            var popUpBg = $('#global-cs-popup-background');
            popUpBg.css('z-index', parseInt(popUpBg.css('z-index')) - 2);
        }
        if (p.callBack != undefined && ret != undefined) {
            p.callBack(ret);
        }
    },
    
	emptyPage: function(page){
    	page.find('.cs_navcontainer').each(function(){
			var self = $(this);
			$.cs.historyStack[self.attr('id')] = [];
		});
    },
	
    removePage: function(page){
    	$.cs.emptyPage(page);
		page.remove();
    },
    
    topContainerId: 'global-content',
    currentNavContainerId: null,
    historyStack: [],
    gotoPage: function(o) {
		if(o.popPreviousPage){
			var containerId = null;
			if(o.containerId){
				containerId = o.containerId;
			}else{
				containerId = $.cs.topContainerId;
			}
			var history = $.cs.historyStack[containerId];
			if(history.length > 0){
				var p = history.pop();
				$.cs.removePage($('#' + p.pageId));
			}
		}
		var html = '';
		if (o.source == 'asset') {
			html = local.readAssetFile(o.url);
			$.cs.addPage({containerId: o.containerId, url: o.url, html: html});
		} else {
			$.cs.ajaxGet({
				url: o.url,
				success: function(html) {
					$.cs.addPage({containerId: o.containerId, url: o.url, html: html});
				}
			});
		}
	},
    addPage: function(o) {
        var containerId = null;
        if(o.containerId){
            containerId = o.containerId;
        }else{
            containerId = $.cs.topContainerId;
        }
        var page = $('<div></div>');
		if(containerId == $.cs.topContainerId){
			page.addClass('fullfill');
		}
		
        var pageId = $.cs.newGuid();
        page.attr('id', pageId);
        page.attr('url', o.url);
        var history = $.cs.historyStack[containerId];
        if(history.length > 0){
            var lastPage = $('#' + history[history.length - 1].pageId);
            lastPage.removeClass('active');
            lastPage.hide();
        }
        page.addClass('active');
        history.push({ type: 'page', pageId: pageId });
        $.cs.currentNavContainerId = containerId;
        $('#' + containerId).append(page);
        page.html(o.html);
        $.cs.processJsForPage(page);
    },
    goBack: function(o) {
    	var popUpBg = $('#global-cs-popup-background');
        if (popUpBg.is(':visible')){
        	//$.cs.closePopup();
            return;
		}
		
        var containerId = $.cs.currentNavContainerId;
        var history = $.cs.historyStack[containerId];
        while(containerId != $.cs.topContainerId && history.length < 2){
            containerId = $('#' + containerId).parents('.cs_navcontainer')[0].id;
            history = $.cs.historyStack[containerId];
        }
        $.cs.currentNavContainerId = containerId;
        var canBack = false;
        if(history.length > 1){
            canBack = true;
        }
        if (canBack) {
            var p = history.pop();
            var page = $('#' + p.pageId);
            $.cs.removePage(page);
            
            p = history[history.length - 1];
            page = $('#' + p.pageId);
            page.show();
            page.addClass('active');
            
            if(o && o.refresh){
            	var refreshTriggerEle = page.find('.refreshtrigger');
                if(refreshTriggerEle.length > 0){
                	if(o.refreshPara){
                		refreshTriggerEle.data('refreshPara', o.refreshPara);
                	}
                    refreshTriggerEle.trigger('click');
                }
            }
        } else {
            local.exit();
        }
    },
	
	cbProgress: function(r){
		$('#global-progressbar').css('width', (parseInt(r) * 4) + 'px');
	},
	startProgress: function(){
		var win = $(window);
		var popUpBg = $('#global-cs-popup-background');
		if (popUpBg.is(':hidden')) {
			popUpBg.height(win.height() + 'px');
			popUpBg.width(win.width() + 'px');
			popUpBg.show();
		} else {
			popUpBg.css('z-index', parseInt(popUpBg.css('z-index')) + 2);
		}
		
		var divProgress = $('#div-global-progress');
		divProgress.css({
			'z-index': parseInt(popUpBg.css('z-index')) + 1, 
			'top': ((win.height() - 40) / 2) + 'px', 
			'left': ((win.width() - 400) / 2) + 'px'
		});
		divProgress.show();
	},
	endProgress: function(){
		$('#global-progressbar').css('width', '0px');
        $('#div-global-progress').hide();
		var popUpBg = $('#global-cs-popup-background');
		popUpBg.hide();
	},
    cbDownloadComplete: null,
    downloadFile: function(o) {
		$.cs.cbDownloadComplete = o.complete;
		$.cs.startProgress();
        local.downloadFile(o.url, o.despath);
    },
    cbUploadComplete: null,
    uploadFile: function(o) {
        $.cs.cbUploadComplete = o.complete;
        $.cs.startProgress();
        local.uploadFile(baseUrl + '/common/uploadfile.htm', o.path);
    },
    
    processJsForPage: function(pageEle){
        $.cs.bindPageEvents(pageEle);
        //Process other UI effects, 'tab', validator, dropdown, etc.
    },

    bindPageEvents: function(pageEle) {
        pageEle.find('.assetpagelink').click(function() {
            var self = $(this);
            $.cs.gotoPage({ containerId: self.closest('.cs_navcontainer').attr('id'), source: 'asset', url: self.data('url') });
        });

        pageEle.find('.pagelink').click(function() {
            var self = $(this);
            var navContainerId;
            if(self.data('navcontainerid')){
            	navContainerId = self.data('navcontainerid');
            }else{
            	navContainerId = self.closest('.cs_navcontainer').attr('id');
            }
            $.cs.gotoPage({ containerId: navContainerId, source: 'web', url: self.data('url') });
        });

        pageEle.find('.submit').click(function(){
            var self = $(this);
            if(self.hasClass('needconfirm')){
                $.cs.showConfirm({
                    message: '确认进行该操作吗？',
                    cbYes: function(){
                        $.cs.submitButton({
                            button: self
                        });
                    }
                });
            }else{
                $.cs.submitButton({
                    button: self
                });
            }
        });
        pageEle.find('.cs_navcontainer').each(function(){
            var self = $(this);
            if(isNullOrEmpty(self.attr('id'))){
                self.attr('id', $.cs.newGuid());
            }
            $.cs.historyStack[self.attr('id')] = [];
			if(self.data('initurl')){
        		$.cs.gotoPage({url: self.data('initurl'), containerId: self.attr('id')});
        	}
        });
        pageEle.find('.cs_tabpage').each(function(){
			$.cs.bindTabPage($(this));
		});
		
		$.cs.bindAjaxLoad(pageEle);
		$.cs.bindInitLoad(pageEle);
    },
    
    bindTabPage: function(p){
		var firstTab = $(p.find('.tabpage')[0]);
		var navCon = firstTab.find('.cs_navcontainer');
		if(navCon.length > 0){
			$.cs.gotoPage({url: navCon.data('url'), containerId: navCon.attr('id')});
			firstTab.data('loaded', true);
		}
		
		p.find('.tabitem').click(function(){
			var self = $(this);
			var pages = self.closest('.cs_tabpage').find('.tab_pages');
			var cudPageId = 1;
			if(pages.data('curPage')){
				cudPageId = pages.data('curPage');
			}
			if(self.data('id') == cudPageId){
                if(self.hasClass('showfirstonclick')){
                    var pageTab = pages.find('.tabpage[data-id=' + self.data('id') + ']');
                    var navContainer = pageTab.find('.cs_navcontainer');
                    $.cs.gotoFirstPageInNavContainer(navContainer);
                }else {
                    return;
                }
			}else{
				self.parent().find('.tabitem').removeClass('current');
				self.addClass('current');
				
				pages.find('.tabpage').hide();
				var pageTab = pages.find('.tabpage[data-id=' + self.data('id') + ']');
				pageTab.show();
				var navContainer = pageTab.find('.cs_navcontainer');
				if(!pageTab.data('loaded')){
					$.cs.gotoPage({url: navContainer.data('url'), containerId: navContainer.attr('id')});
					pageTab.data('loaded', true);
				}else{
					$.cs.currentNavContainerId = navContainer.attr('id');
                    if(self.hasClass('showfirstonclick')){
                        var navContainer = pageTab.find('.cs_navcontainer');
                        $.cs.gotoFirstPageInNavContainer(navContainer);
                    }
				}
				pages.data('curPage', self.data('id'));
			}
		});
	},
	
	ajaxLoadPage: function(o){
        $.cs.ajaxGet({
            url: o.url,
            success: function(htmldata){
				$.cs.emptyPage(o.targetContainer);
                o.targetContainer.html(htmldata);
                $.cs.processJsForPage(o.targetContainer);
            }
        });
    },

    gotoFirstPageInNavContainer: function(navContainer){
        var history = $.cs.historyStack[navContainer.attr('id')];
        while(history.length > 1){
            var p = history.pop();
            $.cs.removePage($('#' + p.pageId));
        }
        if(history.length == 1){
            var p = history[0];
            var page = $('#' + p.pageId);
            page.show();
            page.addClass('active');
        }
    },
	
	bindAjaxLoad: function(pageEle){
		pageEle.find('.ajaxload').click(function(){
			var self = $(this);
			var refreshUrl = self.data('url');
			if(!refreshUrl){
				refreshUrl = self.closest('form').attr('action');
			}
			var targetContainer;
			if(self.data('targetid')){
				targetContainer = $('#' + self.data('targetid'));
			}else{
				targetContainer = self.closest('.cs_page').parent();
			}
			$.cs.ajaxLoadPage({
				url: refreshUrl,
				targetContainer: targetContainer
			});
		});
	},
	
	bindInitLoad: function(pageEle){
		pageEle.find('.initload').each(function(){
            var self = $(this);
            var targetContainer;
            if(self.data('targetid')){
            	targetContainer = $('#' + self.data('targetid'));
            }else{
            	targetContainer = self;
            }
            $.cs.ajaxLoadPage({
                url: self.data('url'),
                targetContainer: targetContainer
            });
        });
	},

    setFormHiddenValue: function(form, id, val){
        var ele = form.find('#' + id);
        if(ele.length == 0){
            form.append($('<input type="hidden" name="' + id + '" id="' + id + '" />'));
            ele = form.find('#' + id);
        }
        ele.val(val);
    },
    submitButton: function(cfg){
		var btn = cfg.button; 
		var form;
		if(cfg.form){
			form = cfg.form;
		}else{
			form = btn.closest('form');
		}
		if(btn.data('command')){
			$.cs.setFormHiddenValue(form, 'command', btn.data('command'));
			if(btn.data('cmdarg')){
				$.cs.setFormHiddenValue(form, 'commandarg', btn.data('cmdarg'));
			}
		}
		if(btn.data('funcbeforesubmit')){
			eval(btn.data('funcbeforesubmit'));
		}       
		$.cs.ajaxPostForm({
			form: form,
			success: function(data){
				if(cfg.success){
					cfg.success(data);
				}else{
					if(btn.hasClass('refresh')){
					
					}else if(btn.hasClass('reload')){
						var reloadTarget = form.closest('.cs_page').parent();
	                    reloadTarget.html(data);
	                    $.cs.processJsForPage(reloadTarget);
					}else if(btn.hasClass('pageback')){
						if(btn.hasClass('backrefresh')){
							$.cs.goBack({
								refresh: 1,
								refreshPara: data.refreshPara
							});
						}else{
							$.cs.goBack();
						}
					}else{
						$.cs.processDefaultPostBack({data: data, containerId: btn.closest('.cs_navcontainer').attr('id')});
					}
				}
			}
		});
	},

    validateForm: function(f) {
        var valid = true;
        var invalidObj;
        var invalidMsg = '';
        f.find('input,textarea').each(function() {
            if (!valid)
                return;

            var self = $(this);
            if (self.attr('type') != 'text' && self.attr('type') != 'password') {
                return;
            }
            if (!self.data('val')) {
                return;
            }
            if (self.data('val-required') != undefined) {
                if (isNullOrEmpty(self.val())) {
                    var msg = self.data('val-required');
                    if (isNullOrEmpty(msg)) {
                        msg = self.attr('placeholder');
                    }
                    if (isNullOrEmpty(msg)) {
                        msg = '必填项未填写';
                    }
                    invalidMsg = msg;
                    invalidObj = self;
                    valid = false;
                    return;
                }
            }
        });
        if (!valid) {
            //invalidObj.focus();
            $.cs.showMessage(invalidMsg);
        }
        return valid;
    },

    ajaxPostForm: function(cfg){
        var form = cfg.form;
        var valid = $.cs.validateForm(form);
        if (!valid)
            return;
        
        $.cs.ajaxPost({
            url: form.attr('action'),
            data: form.serialize(),
            success: function (data) {
                if(data.errormessage && data.errormessage != ''){
                    $.cs.showMessage(data.errormessage);
                }
                else if(cfg.success){
                    cfg.success(data);
                }else{
                    $.cs.processDefaultPostBack({data: data});
                }
            }
        });
    },

    processDefaultPostBack: function(o){
		var data = o.data;
		if(data.message && data.message != ''){
			$.cs.showMessageBox({
				message: data.message,
				cbOk: function(){
					if(data.redirecturl){
						$.cs.gotoPage({url: data.redirecturl, containerId: o.containerId, popPreviousPage: data.popPreviousPage});
					}
				}
			});
		}else if(data.redirecturl){
			$.cs.gotoPage({url: data.redirecturl, containerId: o.containerId, popPreviousPage: data.popPreviousPage });
		}else {
			$.cs.showMessage('操作成功');
		}
	},
    
    cssAppended: false,
    appendCss: function(url){
    	if($.cs.cssAppended)
    		return;
		$($('head')[0]).append($('<link href="' + url + '" rel="stylesheet" type="text/css" />'));	
    	$.cs.cssAppended = true;
    },

    init: function() {
        var loadingWindows = $('#global-loading-window');
        if (loadingWindows.length == 0) {
            $('body').append($('<div id="global-loading-window"><img style="width:60px;height:60px;" src="file:///android_asset/Content/cs/images/loading.gif" /></div>'));
        }
        var divGlobalAlert = $('#divGlobalAlert');
        if (divGlobalAlert.length == 0) {
            $('body').append($('<div class="cshidden"><div id="divGlobalAlert"><div class="message"></div><div class="buttons"><a id="globalBtnMessageOk" class="btn btn-default">确定</a></div></div></div>'));
        }
        var divGlobalAlert = $('#divGlobalConfirm');
        if (divGlobalAlert.length == 0) {
            $('body').append($('<div class="cshidden"><div id="divGlobalConfirm"><div class="message"></div><div class="buttons"><a id="globalBtnConfirmYes" class="btn btn-default">是</a>&nbsp;&nbsp;<a id="globalBtnConfirmNo" class="btn btn-default">否</a></div></div></div>'));
        }
        var divGlobalMenu = $('#divGlobalMenu');
        if (divGlobalMenu.length == 0) {
            $('body').append($('<div id="divGlobalMenu" class="cshidden"><div class="menuitems"></div><div class="cancel">取消</div></div>'));
        }
        var csPopupBackground = $('#global-cs-popup-background');
        if (csPopupBackground.length == 0) {
            $('body').append($('<div id="global-cs-popup-background" style="display: none;"></div>'));
        }
        var csGlobalProgress = $('#div-global-progress');
        if(csGlobalProgress.length == 0){
            $('body').append($('<div id="div-global-progress" style="display: none;"><div id="global-progressbar"></div></div>'));
        }
        
        $('#divGlobalMenu .cancel').click(function(){
        	$('#divGlobalMenu').hide();
        	$('#global-cs-popup-background').hide();
        });
        $.cs.bindPageEvents($('body'));
        $.cs.currentNavContainerId = $.cs.topContainerId;
        currentVer = local.getVersion();
        baseUrl = getConfig('baseUrl');
        if (isNullOrEmpty(baseUrl)) {
            //baseUrl = 'http://10.10.10.66:8087/ydworklog';
            //saveConfig('baseUrl', baseUrl);
        }
        if (getConfig('isDebugMode') == 'true') {
            isDebugMode = true;
        }
        machineId = local.getMachineId();
        csInited = true;
    },
    
    showMenu: function(o){
		var win = $(window);
		var menuObj = $('#divGlobalMenu');
		var menuItemsObj = menuObj.find('.menuitems');
		menuItemsObj.empty();
		for(var i=0;i<o.items.length;i++){
			var itemCfg = o.items[i];
			var item = $('<div class="item" data-idx="' + i + '">' + itemCfg.text + '</div>');
			menuItemsObj.append(item);
		}
		if(!menuItemsObj.data('eventAttached')){
			menuItemsObj.data('eventAttached', true);
			menuItemsObj.on('click', '.item', function(){
				var self = $(this);
				var idx = self.data('idx');
				o.items[idx].cb();
			});
		}
		
		var popUpBg = $('#global-cs-popup-background');
        if (popUpBg.is(':hidden')) {
            popUpBg.height(win.height() + 'px');
            popUpBg.width(win.width() + 'px');
            popUpBg.show();
        }
        menuObj.css('z-index', parseInt(popUpBg.css('z-index')) + 1);
		menuObj.show();
	},
	hideMenu: function(){
		var menuObj = $('#divGlobalMenu');
		menuObj.hide();
		var popUpBg = $('#global-cs-popup-background');
		popUpBg.hide();
	}
});

function loadAfterCsInited(callBack) {
    $.cs.init();
    csWait({
        when: function() {
            return csInited;
        },
        work: function() {
            callBack();
        }
    });
}
$(function() {
    loadAfterCsInited(pageload);
});