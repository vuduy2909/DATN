const mysql = require('mysql');

const con = mysql.createConnection({
    host: "localhost",
    user: "YOUR_MYSQL_USERNAME",
    password: "YOUR_MYSQL_PASSWORD",
    database: 'wellness_navigator_gym'
});

con.connect(function (err) {
    if (err) throw err;
    con.query("SELECT * FROM tasks", function (err, result, fields) {
        if (err) throw err;
        console.log(result);
    });
});