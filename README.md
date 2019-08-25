# U of U CS Bot
[![GitHub license](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://raw.githubusercontent.com/mitchtalmadge/uofu-cs-bot/master/LICENSE)
[![Build Status](https://travis-ci.org/MitchTalmadge/UofU-CS-Bot.svg?branch=master)](https://travis-ci.org/MitchTalmadge/UofU-CS-Bot)
[![GitHub issues](https://img.shields.io/github/issues/MitchTalmadge/UofU-CS-Bot.svg)](https://github.com/MitchTalmadge/UofU-CS-Bot/issues)

<img src="http://i.imgur.com/yQYPYFh.png" width="100px" align="left"/>

The U of U CS Bot is a Discord bot that is used to automatically organize a large Computer Science chatroom. 

This bot automatically creates, deletes, and organizes private channels for every CS class, with automatic role-assignment based on users' nicknames.

For example, a user named `Mitch [2420-TA, 3505, 3810]` would gain access to the CS-2420, CS-3505, and CS-3810 class channels. Additionally, since he is a TA, the user would be able to kick users and delete messages from the CS-2420 channel.

The bot also has the ability to manage private club channels, where entire clubs can meet together on Discord in their own channels. Users can join clubs using text commands, such as `!club join acm`.

## Architecture
The U of U CS Bot is build upon the [Spring Boot](https://github.com/spring-projects/spring-boot) framework. 
Spring Boot provides a great underlying framework for building new features quickly and with as few errors as possible. 

The Bot uses [my own custom HSQLDB docker image](https://github.com/MitchTalmadge/hsqldb-dockerized) to store user verification data, and an STMP server to send verification emails.

