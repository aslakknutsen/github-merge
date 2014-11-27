<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8" />
    <title>Traveling Merger</title>
    <base href="<%=request.getAttribute("BASE_ROOT")%>" />
    <meta content="width=device-width, initial-scale=1.0" name="viewport" />
    <!--[if lt IE 9]>
      <script src='//html5shim.googlecode.com/svn/trunk/html5.js' type='text/javascript'></script>
    <![endif]-->
  </head>
  <body>

	<script type="text/x-handlebars" data-template-name="commits">
		<div class="icon-bar one-up">
  			<a class="item right">
    			<i class="fa fa-question-circle"></i>
  			</a>
		</div>
		{{notification-area resource=resource}}
		<div class="row">
			<ul class="button-group right">
				<li><a title="Pick all" {{action 'pickAll'}} class="button tiny">Pick all</a></li>
				<li><a title="Fixup all" {{action 'fixupAll'}} class="button tiny">Fixup all</a></li>
			</ul>
		</div>
		<div class="row">
          <ol class="commits">
          {{#each commit in model}}
		      <li class="commit row">
               {{single-commit commit=commit moveup="moveup"}}
            </li>
		    {{/each}}
          </ol>
		</div>
		<div class="row">
			<ul class="button-group right">
				<li><a title="Push changes upstream" class="button small alert">Push</a></li>
				<li><a title="Perform rebase" {{action 'rebase'}} class="button small">Rebase</a></li>
			</ul>
		</div>
	</script>

	<script type="text/x-handlebars" data-template-name="components/single-commit">
	  <div class="small-1 column">
		<div class="avatar">
            <img src="" />
         </div>
      </div>
	  <div class="commit">
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
		  <div class="small-3 column actions right text-right">
			 <ul class="button-group">
				{{#unless isFirst}}
					<li><i title="Move up" {{action 'moveup'}} class="button tiny fa fa-arrow-up"></i></li>
					<li><i title="Fixup commit" {{action 'fixup'}} class="button tiny fa fa-caret-square-o-up"></i></li>
				{{/unless}}
				<li><i title="Edit message" {{action 'edit'}} class="button tiny fa fa-edit"></i></li>
				<li><i title="Remove commit" {{action 'remove'}} class="button tiny fa fa-trash-o"></i></li>
			 </ul>
		  </div>
		  {{/if}}
	  </div>
		
	</script>

	<script type="text/x-handlebars" data-template-name="components/notification-area">
		<div class="row logs">
			<div {{action 'toggleVisibility'}}>
				<i title="Show/Hide" class="fa fa-plus"></i> Logs <small>({{notifications.length}})</small>
			</div>
			<ul {{bind-attr class="hide"}}>
				{{#each msg in notifications}}
					<li>{{msg}}</li>
				{{/each}}
			</ul>
		</div>
	</script>
  </body>

  <script src="//cdnjs.cloudflare.com/ajax/libs/jquery/2.1.1/jquery.min.js"></script>
  <script src="//cdnjs.cloudflare.com/ajax/libs/handlebars.js/2.0.0/handlebars.min.js"></script>
  <script src="//cdnjs.cloudflare.com/ajax/libs/ember.js/1.9.0-beta.3/ember.min.js"></script>
  <!--<script src="//cdnjs.cloudflare.com/ajax/libs/ember-data.js/1.0.0-beta.11/ember-data.js"></script>-->
  <script src="//cdnjs.cloudflare.com/ajax/libs/foundation/5.4.7/js/foundation.min.js"></script>
  <link href="//cdnjs.cloudflare.com/ajax/libs/foundation/5.4.7/css/foundation.min.css" rel="stylesheet"></style>
  <link href="//cdnjs.cloudflare.com/ajax/libs/font-awesome/4.2.0/css/font-awesome.min.css" rel="stylesheet"></style>
  <style>
    .icon-bar {
      background-color: #fff; 
    }
    .icon-bar>* i {
      color: #000;
    }
    .is-fixup {
		background-color: yellow;
    }
    .is-delete {
    	background-color: red;
    }
    a {
      color: #000;
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

    .meta {
    	font-size: 0.8em;
    }
    .title {
    	font-weight: bold;
    	margin-bottom: 0px;
    }
  </style>
  <script>
	App = Ember.Application.create();

	App.Router.reopen({
		location: 'history'
	});

	App.Router.map(function() {
		this.resource('commits', { path: '/:user/:repository/:pullrequest' });
	});
	var base = '<%=request.getAttribute("BASE_ROOT")%>/api/rebase/';
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
			data: JSON.stringify(model)
		});
	};

	App.CommitsRoute = Ember.Route.extend({
		model: function(params) {
			return getStatus(params);
		},
		setupController: function(controller, model) {
			var params = this.paramsFor(this.routeName);
			controller.set('model', model);
			controller.set('resource', params);
		}
	});
	
	App.CommitsController = Ember.ArrayController.extend({
		resource: null, // set in route
    	assignIndex: function() {
    		//Ember.run.once(this, 'updateIndex');
    		this.updateIndex()
        }.observes('model.[]'),
        updateIndex: function() {
			this.map(function(item, index) {
				if(item !== undefined) {
					//console.log(index+1 + ":" + item.id + " update")
					Em.set(item,'_index',index+1)
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
		actions: {
			moveup: function() {
				this.sendAction('moveup', this.get('commit'))
			},
			remove: function() {
				if(this.get('commit.state') === 'DELETE') {
					this.set('commit.state', 'PICK')
				} else {
					this.set('commit.state', 'DELETE');
				}
			},
			fixup: function() {
				if(this.get('commit.state') === 'FIXUP') {
					this.set('commit.state', 'PICK')
				} else {
					this.set('commit.state', 'FIXUP');
				}
			},
			edit: function() {
				this.set('msg', this.get('commit.message'));
				this.set('isEdit', true);
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
			//console.log(this.get('commit._index') + ":" + this.get('commit.id') + ":" + this.get('isFirst'))
			if(this.get('isFirst')) {
				this.set('commit.state', 'PICK');
			}
		}.observes('commit._index'),
		isDelete: function() {return this.get('commit.state') === 'DELETE'}.property('commit.state', 'commit._index'),
		isFixup: function() {return this.get('commit.state') === 'FIXUP'}.property('commit.state', 'commit._index'),
		isFirst: function() {
			return this.get("commit._index") == 1;
		}.property('commit._index')

	});

	App.NotificationAreaComponent = Ember.Component.extend({
		resource: null, // set in controller
		socket: null,
		notifications: [],
		reconnectCount: 0,
		hide: true,
		init: function() {
			this.open();
			return this._super();
		},
		open: function() {
			var self = this;
			var resource = this.get('resource');
			this.socket = new WebSocket('ws://' + window.location.host + '<%=request.getAttribute("BASE_ROOT")%>/api/' + resource.user + '/' + resource.repository + "/" + resource.pullrequest + '/notification')
			this.socket.onopen = function() {
				self.onOpen();
			}
			this.socket.onmessage = function(event) {
				self.onMessage(event.data);
			}
			this.socket.onclose = function() {
				self.onClose();
			}
		},
		onMessage: function(data) {
			this.get('notifications').pushObject(data);
		},
		onClose: function() {
			if(this.get('reconnectCount') < 10) {
				this.set('reconnectCount', this.get('reconnectCount')+1);
				this.open();
			}
		},
		onOpen: function() {
			this.set('reconnectCount', 0)
		},
		actions: {
			toggleVisibility: function() {
				if(this.get('hide')) {
					this.set('hide', false);
				} else {
					this.set('hide', true);
				}
			}
		}
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
	});

	var commits = Em.A([
			App.Commit.create({
				id: 'A',
				message: 'description 1\nlong message\n',
				state: 'PICK',
				author: 'aslak@4fs.no'})
			,
			App.Commit.create({
				id: 'B',
				message: 'description 2\nlong message\n',
				state: 'PICK',
				author: 'aslak@4fs.no'})
				,
			App.Commit.create({
				id: 'C',
				message: 'description 3\nlong message\n',
				state: 'PICK',
				author: 'aslak@4fs.no'})
		]);
  </script>
</html>