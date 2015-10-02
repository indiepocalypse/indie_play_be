const Browser = require('./zombie/');

login = process.argv[2];
pssw = process.argv[3];
url = process.argv[4];

browser = new Browser();

browser.visit(url, {element:".container"}, function () {
	console.log("title="+browser.document.title);

	browser.fill('#login_field',login);
		browser.fill('#password',pssw);
	browser.click('input.btn', function() {});
	browser.wait({element:".container"}, function() {
  		console.log("title="+browser.document.title);
	});
	// TODO: handle unsuccesful tyransfer (at least notify the java app...

});

