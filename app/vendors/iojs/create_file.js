const Browser = require('./zombie/');

url_login = "https://github.com/login"
login = process.argv[2];
pssw = process.argv[3];
url = process.argv[4];
file_name = process.argv[5];
content = process.argv[6];

browser = new Browser();

browser.visit(url_login, {element:".container"}, function () {
	console.log("title="+browser.document.title);
	browser.fill('#login_field',login);
	browser.fill('#password',pssw);
	browser.click('input.btn', function() {});
	browser.wait({element:".container"}, function() {
  		console.log("title="+browser.document.title);
		browser.visit(url, {element:".container"}, function () {
			browser.wait({element:".container"}, function() {
				browser.fill('input.js-blob-filename', file_name);
				browser.fill('textarea', content);
				browser.click('#submit-file', function() {});
				browser.wait({element:".container"}, function() {
					console.log("title="+browser.document.title);
				});
			});
		});
	});
	// TODO: handle unsuccesful transfer (at least notify the java app...
});

