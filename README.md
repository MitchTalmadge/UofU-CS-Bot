# U of U CS Bot
[![GitHub license](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://raw.githubusercontent.com/mitchtalmadge/uofu-cs-bot/master/LICENSE)
[![Build Status](https://travis-ci.org/MitchTalmadge/UofU-CS-Bot.svg?branch=master)](https://travis-ci.org/MitchTalmadge/UofU-CS-Bot)
[![GitHub issues](https://img.shields.io/github/issues/MitchTalmadge/UofU-CS-Bot.svg)](https://github.com/MitchTalmadge/UofU-CS-Bot/issues)

<img src="http://i.imgur.com/3KUsJiF.png" width="100px" align="left"/>

The U of U CS Bot is a simple Discord bot for a specific University of Utah Computer Science chat-room.

Currently, the sole purpose of the bot is to manage roles based on users' nicknames. This was created to organize the server and remove the hassle of managing who is in which CS classes.

## Architecture
The U of U CS Bot is build upon the [Spring Boot](https://github.com/spring-projects/spring-boot) framework. 
Spring Boot is not fully necessary for the functionality of this bot, but provides a great underlying framework for building new features quickly and with as few errors as possible. 

Hosting is provided by [Heroku](https://www.heroku.com/) on a free tier dyno.

