<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8" />
    <title>Traveling Merger</title>
    <base href="<%=request.getAttribute("BASE_ROOT")%>" />
    <meta name="viewport" content="user-scalable=no, width=device-width, initial-scale=1, maximum-scale=1">
    <!--[if lt IE 9]>
      <script src='//html5shim.googlecode.com/svn/trunk/html5.js' type='text/javascript'></script>
    <![endif]-->
  </head>
  <body>

	<script type="text/x-handlebars">
		<!--div class="icon-bar one-up">
  			<a class="item right">
    			<i class="fa fa-question-circle"></i>
  			</a>
		</div-->
		{{outlet}}
	</script>

	<script type="text/x-handlebars" data-template-name="pullrequest/loading">
		<div class="row">
			<div class="small-8 column">
				Loading..
			</div>
		</div>
	</script>

	<script type="text/x-handlebars" data-template-name="user">
		{{outlet}}
	</script>
	<script type="text/x-handlebars" data-template-name="repository">
		{{outlet}}
	</script>
	<script type="text/x-handlebars" data-template-name="pullrequest">
		<div class="row logs">
			{{#if inProgress}}
			<p>{{progressCategory}}</p>
			<div {{bind-attr class=":progress :round progressStatus:success:alert"}}>
				<span class="meter" {{bind-attr style="meterStyle"}}></span>
			</div>
			{{/if}}
			<div {{action 'toggleNotification'}}>
				<i title="Show/Hide" class="fa fa-plus"></i> Logs <small>({{model.length}})</small>
			</div>
			<ul {{bind-attr class="hideNotification:hide"}}>
				{{#each model}}
					<li>{{this}}</li>
				{{/each}}
			</ul>
		</div>
		{{outlet}}
	</script>
	<script type="text/x-handlebars" data-template-name="pullrequest/index">
		{{#if model}}
		<div class="row">
			<div class="small-8 small-centered columns text-center">
				<ul class="button-group">
					<li><a title="Pick all" {{action 'pickAll'}} class="button tiny">Pick all</a></li>
					<li><a title="Fixup all" {{action 'fixupAll'}} class="button tiny">Fixup all</a></li>
				</ul>
			</div>
		</div>
		{{/if}}
		<div class="row">
			<div class="small-12 column">
				<ol class="commits">
				{{#each commit in model}}
					<li class="commit row">
						{{single-commit commit=commit moveup="moveup"}}
					</li>
				{{else}}
					<li>All commits on branch are upstream. Nothing to do!</li>
				{{/each}}
				</ol>
			</div>
		</div>
		{{#if model}}
		<div class="row">
			<div class="small-8 small-centered columns text-center">
				<ul class="button-group">
					<li><a title="Push changes upstream" {{action 'push'}} class="button tiny alert">Push</a></li>
					<li><a title="Perform rebase" {{action 'rebase'}} class="button tiny">Rebase</a></li>
				</ul>
			</div>
		</div>
		{{/if}}
	</script>

	<script type="text/x-handlebars" data-template-name="components/single-commit">
		<div>
			<div class="small-1 column">
				<div class="avatar">
					<img src="" />
				</div>
			</div>
			{{#if isEdit}}
			<div class="small-8 column">
				{{textarea value=msg rows=10}}
			</div>
			<div class="small-3 column actions right text-right">
				<ul class="button-group">
					<li><i title="Save change" {{action 'saveEdit'}} class="button tiny fa fa-save"></i></li>
					<li><i title="Discard change" {{action 'closeEdit'}} class="button tiny fa fa-close"></i></li>
				</ul>
			</div>
			{{else}}
			<div {{bind-attr class=":small-8 :column isDelete isFixup"}}>
				<p class="title">{{get-message-head commit.message}}</p>
				<div class="meta">
					{{commit.author}} authored <time>{{commit.date}}</time>
				</div>
				<div class="desc">
					<pre>{{get-message-body commit.message}}</pre>
				</div>
			</div>
			<div class="small-3 column actions text-right">
				<ul class="button-group">
				{{#unless isFirst}}
					<li><i title="Move up" {{action 'moveup'}} class="button tiny fa fa-arrow-up"></i></li>
					<li><i title="Fixup commit" {{action 'fixup'}} class="hide-for-small-only button tiny fa fa-caret-square-o-up"></i></li>
					<li><i title="Remove commit" {{action 'remove'}} class="hide-for-small-only button alert tiny fa fa-trash-o"></i></li>
				{{/unless}}
					<li><i title="Edit message" {{action 'edit'}} class="hide-for-small-only button tiny fa fa-edit"></i></li>
				</ul>
			</div>
			{{/if}}
		</div>
	</script>
  </body>

  <script src="//cdnjs.cloudflare.com/ajax/libs/jquery/2.1.1/jquery.min.js"></script>
  <script src="//cdnjs.cloudflare.com/ajax/libs/handlebars.js/2.0.0/handlebars.min.js"></script>
  <script src="//cdnjs.cloudflare.com/ajax/libs/ember.js/1.9.0-beta.3/ember.min.js"></script>
  <!--<script src="//cdnjs.cloudflare.com/ajax/libs/ember-data.js/1.0.0-beta.11/ember-data.js"></script>-->
  <script src="//cdnjs.cloudflare.com/ajax/libs/hammer.js/1.1.3/hammer.min.js"></script>
  <script src="/merger/app/js/ember-hammer.js"></script>
  <script src="//cdnjs.cloudflare.com/ajax/libs/foundation/5.4.7/js/foundation.min.js"></script>
  <link href="//cdnjs.cloudflare.com/ajax/libs/foundation/5.4.7/css/foundation.min.css" rel="stylesheet"></style>
  <link href="//cdnjs.cloudflare.com/ajax/libs/font-awesome/4.2.0/css/font-awesome.min.css" rel="stylesheet"></style>

  <script>
	App = Ember.Application.create({
		//LOG_TRANSITIONS: true,
		//LOG_TRANSITIONS_INTERNAL: true
	});

	App.Router.reopen({
		location: 'history'
	});

	App.Router.map(function() {
		this.resource('user', {path: '/:user'}, function() {
			this.resource('repository', {path: '/:repository'}, function() {
				this.resource('pullrequest', {path: '/:pullrequest'}, function() {

				})
			})
		})
	});
	var base = '<%=request.getAttribute("BASE_ROOT")%>/api/rebase/';
	var removeClientState = function(key, value) {
		if(key.indexOf("$") == 0) {
			return undefined;
		}
		return value;
	};
	var getStatus = function(resource) {
		//debugger
		var url = base +  resource.user + '/' + resource.repository + '/' + resource.pullrequest;
		return $.getJSON(url).then(function(response) {
			var items = [];
			response.forEach(function(item) {
				items.push(App.Commit.create(item));
			});
			return items;
		});
	}
	var postRebase = function(resource, model) {
		var url = base +  resource.user + '/' + resource.repository + '/' + resource.pullrequest;
		return $.ajax({
			url: url,
			type: 'post',
			contentType: 'application/json',
			data: JSON.stringify(model, removeClientState)
		});
	};

	var postPush = function(resource) {
		var url = base +  resource.user + '/' + resource.repository + '/' + resource.pullrequest + '/push';
		return $.ajax({
			url: url,
			type: 'post'
		});
	};

	App.ApplicationRoute = Ember.Route.extend({
		model: function() {
			console.log('ApplicationRoute.model')

		},
		setupController: function(controller, model) {
			console.log('ApplicationRoute.setup')
			//controller.set('resource', this.paramsFor('commits'));
		}
	});

	App.UserRoute = Ember.Route.extend({
		model: function() {
			console.log('UserRoute.model')
		}
	});
	App.UserController = Ember.ObjectController.extend({

	});
	App.PullrequestRoute = Ember.Route.extend({
		model: function(params) {
			var self = this;
			var deferred = Ember.RSVP.defer();
			var model = [];
			var resource = collectParams(this);
			model.socket = new WebSocket('ws://' + window.location.host + '<%=request.getAttribute("BASE_ROOT")%>/api/' + resource.user + '/' + resource.repository + "/" + resource.pullrequest + '/notification')
			model.socket.onopen = function() {
				//self.onOpen();
				deferred.resolve(model);
			}
			model.socket.onmessage = function(event) {
				var json = JSON.parse(event.data);
				self.get('controller').send('message', json);
			}
			model.socket.onclose = function() {
				//self.onClose();
			}
			return deferred.promise;
		}
	});
	App.PullrequestIndexRoute = Ember.Route.extend({
		model: function(params) {
			console.log('CommitRoute.model ' + collectParams(this))
			return getStatus(collectParams(this));
		},
		setupController: function(controller, model) {
			var params = collectParams(this);
			console.log('CommitRoute.params ' + params)
			controller.set('model', model);
			controller.set('resource', params);
		},
	});

	var collectParams = function(route) {
			return {
				user: route.paramsFor('user').user,
				repository: route.paramsFor('repository').repository,
				pullrequest: route.paramsFor('pullrequest').pullrequest
			}
		}

	App.PullrequestController = Ember.ArrayController.extend({
		progressCategory: '',
		inProgress: false,
		progressStatus: true,
		currrentProgress: 0,
		hideNotification: true,
		meterStyle : function() {
			return 'width: ' + this.get('currrentProgress') + '%;'
		}.property('currrentProgress'),
		actions: {
			toggleNotification: function() {
				this.toggleProperty('hideNotification');
			},
			message: function(message) {
				if(message.type === 'MESSAGE') {
					this.get('model').pushObject(message.message);
				} else if(message.type === 'PROGRESS_START') {
					this.set('inProgress', true);
					this.set('currrentProgress', 0)
					this.set('progressCategory', message.message);
				} else if(message.type === 'PROGRESS') {
					this.set('currrentProgress', parseInt(message.message));
				} else if(message.type === 'PROGRESS_END') {
					this.set('progressStatus', message.message === 'true' ? true:false);
				}
			}
		}
	})

	App.PullrequestIndexController = Ember.ArrayController.extend({
		resource: null, // set in route
    	assignIndex: function() {
    		//Ember.run.once(this, 'updateIndex');
    		this.updateIndex()
        }.observes('model.[]'),
        updateIndex: function() {
			this.map(function(item, index) {
				if(item !== undefined) {
					//console.log(index+1 + ":" + item.id + " update")
					Em.set(item,'$index',index+1)
				}
			})
        },
        actions: {
			moveup: function(commit) {
				var commits = this.get('model');
				var currIndex = commits.indexOf(commit);
				if(currIndex > 0) {
					commits.insertAt(currIndex-1, commit);
					commits.removeAt(currIndex+1);
				}
			},
			rebase: function() {
				var self = this;
				var resource = this.get('resource')
				postRebase(resource, this.get('model')).then(function() {
					getStatus(resource).then(function(model) {
						self.set('model', model);
					}, function(error) {
						console.log(error);
					});
				});
			},
			push: function() {
				var self = this;
				var resource = this.get('resource')
				postPush(resource).then(function() {
					getStatus(resource).then(function(model) {
						self.set('model', model);
					}, function(error) {
						console.log(error);
					});
				});
			},
			fixupAll: function() {
				var commits = this.get('model');
				for(var i = 1; i < commits.length; i++) {
					commits[i].set('state', 'FIXUP');
				}
			},
			pickAll: function() {
				var commits = this.get('model');
				for(var i = 1; i < commits.length; i++) {
					commits[i].set('state', 'PICk');
				}
			}
		}
	});

	App.SingleCommitComponent = Ember.Component.extend({
		isEdit: false,
		msg: '',
		hammerOptions: {
			behavior: {
				userSelect: false
			},
			swipeVelocityX: 0.5
		},
		gestures: {
			swipeLeft: function (event) {
				//console.log('swipeLeft');
				this.send('remove');
				return false;
			},
			swipeRight: function (event) {
				//console.log('swipeRight');
				this.send('fixup');
				return false;
			},
			doubletap: function (event) {
				this.send('edit');
				return false;
			}
		},
		keyDown: function(event) {
			if(event.keyCode == 27) {
				this.send('closeEdit');
			}
		},
		actions: {
			moveup: function() {
				if(this.get('isFirst')) {
					return;
				}
				this.sendAction('moveup', this.get('commit'))
			},
			remove: function() {
				if(this.get('isFirst')) {
					return;
				}
				if(this.get('commit.state') === 'DELETE') {
					this.set('commit.state', 'PICK')
				} else {
					this.set('commit.state', 'DELETE');
				}
			},
			fixup: function() {
				if(this.get('isFirst')) {
					return;
				}
				if(this.get('commit.state') === 'FIXUP') {
					this.set('commit.state', 'PICK')
				} else {
					this.set('commit.state', 'FIXUP');
				}
			},
			edit: function() {
				this.set('msg', this.get('commit.message'));
				this.set('isEdit', true);
				Ember.run.later(function() {
					this.$('textarea').focus();
				})
			},
			closeEdit: function() {
				this.set('msg', '');
				this.set('isEdit', false);
			},
			saveEdit: function() {
				this.set('commit.message', this.get('msg'))
				this.set('msg', '');
				this.set('isEdit', false);
				this.set('commit.state', 'REWORD');
			}
		},
		updateState: function() {
			//console.log(this.get('commit.$index') + ":" + this.get('commit.id') + ":" + this.get('isFirst'))
			if(this.get('isFirst')) {
				this.set('commit.state', 'PICK');
			}
		}.observes('commit.$index'),
		isDelete: function() {return this.get('commit.state') === 'DELETE'}.property('commit.state', 'commit.$index'),
		isFixup: function() {return this.get('commit.state') === 'FIXUP'}.property('commit.state', 'commit.$index'),
		isFirst: function() {
			return this.get("commit.$index") == 1;
		}.property('commit.$index')

	});

	Ember.Handlebars.helper('get-message-head', function(message) {
		if(message !== undefined) {
			return message.split('\n')[0];
		}
		return "";
	});

	Ember.Handlebars.helper('get-message-body', function(message) {
		if(message !== undefined) {
			var lines = message.split('\n');
			lines.shift();
			return lines.join('\n');
		}
		return "";
	});

	App.Commit = Ember.Object.extend({
		id: null,
		message: null,
		state: null,
		author: null
	});
  </script>

  <style>
    .icon-bar {
		background-color: #fff;
    }
    .icon-bar>* i {
		color: #000;
    }
    .is-fixup {
		opacity: 0.5;
		text-decoration: underline;
    }
    .is-delete {
		opacity: 0.5;
		text-decoration: line-through;
    }
    .is-delete .desc, .is-fixup .desc {
		display:none;
    }

    a {
		color: #000;
    }

    ol {
		margin-left: 0px !important;
    }
    .commit {
		background-color: #eee;
    }

	.commits {
		list-style-type: none;
	}

    .commits > li {
		margin-bottom: 5px !important;
		padding-top: 10px;
		padding-bottom: 10px;
    }
	.desc pre {
		white-space: pre-wrap;
	}
    .meta {
		font-size: 0.8em;
    }
    .title {
		font-weight: bold;
		margin-bottom: 0px;
    }
  </style>
</html>