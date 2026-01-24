# Explanation of the project

## Create Spring-Boot Project

Go to [Spring Initializr](https://start.spring.io) website and create a new spring boot project with the following dependencies:
1. WebMvc
2. Thymeleaf
3. Spring Security

## Create new Single Page Application

In order to create a new single page application:
1. Create a new sub-project (gradle module). Let's call it `app1`
2. Add `gradle-node-plugin` - `id("com.github.node-gradle.node") version "7.1.0"`
3. Sync gradle
4. Add `node` configuration to download the local copy of nodejs:
    ```kotlin
    node {
        download = true
        version = "24.12.0"
    }
    ```
5. Build project - `./gradlew build` or `./gradlew processResources`
6. nodejs should be download inside `app1/.gradle` directory
7. Create `3` scripts to invoke local installation of `node`, `npm` and `npx`:
    ```shell
    # content of app1/node
    #!/bin/sh
    cd $(dirname $0)
    PATH="$PWD/.gradle/nodejs/<node-path>/bin":$PATH
    node "$@"
   
    # content of app1/npm
    #!/bin/sh
    cd $(dirname $0)
    PATH="$PWD/.gradle/nodejs/<node-path>/bin":$PATH
    npm "$@"
   
    # content of app1/npx
    #!/bin/sh
    cd $(dirname $0)
    PATH="$PWD/.gradle/nodejs/<node-path>/bin":$PATH
    npx "$@"
    ```
8. Use `npm` or `npx` to create new single page application. For example, to create a new app using vue.js framework inside `app1` module:
   ```shell
   cd app1
   ./npm create vue@latest # Enter the details when asked
   
   # Let's say you named the project `myapp`
   # Then, a new directory named `myapp` is created
   # But, we want to move all the files into the current directory
   mv myapp/* . # Move all visible files
   mv myapp/.* . # Move all files starting with '.'
   ```
9. Don't run `./npm install` or `./npm run build` commands. We need to create gradle task for that.
10. `gradle-node-plugin` already provides a task called `npmInstall`, which runs `npm install`, where `npm` is picked up from the local installation in the project (which we need).
11. Create the following task in `app/build.gradle.kts` to build the project:
   ```kotlin
   val buildWebapp = tasks.register<NpmTask>("buildWebapp") {
       dependsOn("npmInstall")
       args.assign(listOf("run", "build"))
       inputs.files("package.json", "package-lock.json")
       inputs.dir("src")
       inputs.dir(fileTree("node_modules").exclude(".cache"))
       outputs.dir(layout.projectDirectory.dir("dist"))
   }
   ```
12. We want the `:app1:processResources` task to depend on `buildWebapp` task:
   ```kotlin
   tasks.processResources {
       dependsOn(buildWebapp)
   }
   ```

## Configure Root Project

Root project's `processResource` should copy the build output of spa's into `template` build directory:

```kotlin
subprojects {
	afterEvaluate {
		if (tasks.findByName("buildWebapp") != null) {
			val webAppName = project.name
			rootProject.tasks.processResources {
				from(tasks.named("buildWebapp")) {
					into("templates/$webAppName")
				}
			}
		}
	}
}
```

The build files will be copied from the sub-projects' build output directory to `build/resources/main/templates/${appName}` directories. We need to serve the index.html file from these templates for each app. Something like this:

```java
@GetMapping("app1")
String app1() {
 return "app1/index";
}

// If you want to make `app1` as default app
@GetMapping
String defaultGet() {
   return "redirect:/app1";
}
```

## Fixing a few problems

Run the app and go to `http://localhost:8080`.

### Accessing Assets

You will see some errors related to loading of the assets in the network tab. This is due to the fact that by default the app will try to load the assets from root path (ex - `/static/js/index.js`, `/assets/css/main.css`). But the assets are not present at these paths. The assets are present at the app related directory inside `template` directory (ex - `/templates/app1/static/js/index.js`, `/templates/app2/assets/css/main.css` and so on). We need to configure `2` things here:

1. Make the SPA look for assets in correct directories. For example, `app1` should look for assets in related to `app1` directory and `app2` should look for assets in directory related to `app2`. This configuration depends on the build tool used in SPA. For `react-script`, define the property `homepage` in `package.json` file with the correct value (`app1` for example). For `vite` projects, define the property `base` in `vite.config.ts` file with the correct value (`app2` for example).
2. Make the spring look at `templates` directory as well for assets.
   ```properties
   spring.web.resources.static-locations=classpath:/static,classpath:/templates
   ```

### Fixing Routing

In some cases, the routing of SPA changes the relative path in the url. For example, you are at `http://localhost:8080/app1` and you click on a route link for path `/hello` and the url changes to `http://localhost:8080/hello` instead of `http://localhost:8080/app1/hello`. This may not break the app, but has other side effect. In order to fix this, you need to look at the documentation for the routing library you are using. On example where we face this issue is library `react-router-dom`. To fix the issue here, we add `basename` attribute to `BrowserRouter` component, like this:
```html
<BrowserRouter basename="/app1">
    <App />
</BrowserRouter>
```

Some routing libraries may not have this problem or some have other ways to solve this issue.

---

Another problem related to routing can be reproduced by the following steps.
1. Go to `http://localhost:8080/app1`.
2. Navigate to some other route within `app1` by clicking on any route link (`/app1/hello` for example).
3. Reload the page

You will get a `404` error since there is no controller mapping defined for `app1/hello` in the spring application. We need the following:
1. Accessing any resource of pattern `app1/{path}` should just render the `index.html` file for `app1`.
2. Should not change the url. That means, we want to `forward` the request to `app1` not `redirect` to `app1`.
   ```java
   @GetMapping("app1/{path}")
   String app1Paths() {
    return "forward:/app1";
   }
   ```
3. The SPA will take care of the rest by looking at the url for path information.

## Spring Security Configuration

Configuring security for this setup is no different from the regular setup. Since, we are serving the apps on different path, we can apply different security configurations as well for different apps. Consider the following example:

```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
 var mfa = AuthorizationManagerFactories.multiFactor()
     .requireFactors(FactorGrantedAuthority.OTT_AUTHORITY,
         FactorGrantedAuthority.PASSWORD_AUTHORITY)
     .build();

 http.authorizeHttpRequests(
     authz ->
         authz
             .requestMatchers("/app2/**").access(mfa.hasAuthority("ROLE_ADMIN"))
             .anyRequest().authenticated()
 );
 

 http.formLogin(Customizer.withDefaults());
 http.oneTimeTokenLogin(Customizer.withDefaults());

 return http.build();
}
```

In the above configuration, access to `app2` and child routes require the user to be authenticated with multiple authentication factors (password and ott) and use should have `ADMIN` role. Whereas, `app` can be accessed by any authenticated user.

## Accessing Backend Resources

SPAs can access the backend resources just as regular apps. Consider the following JavaScript code-snippet which sends the request to `/resources/me` endpoint.

```javascript
const sayHello = async () => {
  const details = await fetch('/resources/me');
  const me = await details.json();
  console.log({me});
  setUsername(me.username);
};
```

While sending the request, you don't need to worry about the session management. All the cookies are sent automatically by the browser and session is maintained by the server. And your resource controller does not need to worry about the origin of the request.

### Sending Mutating Request

By default, you can not send pos / put / patch / delete kind of requests due to **CSRF Protection** provided by Spring Security. You need to configure the `csrf` in Spring Security for Single Page Applications. There is a convenient way to do that:

```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
   // ... other configurations

   // CSRF Configuration for single-page-applications
   http.csrf(csrf -> csrf.spa());
   
   // ... other configurations

   return http.build();
}
```

This will store the CSRF token in a cookie names `XSRF-TOKEN`. In order to validate the CSRF token, it will read the value of header `X-XSRF-TOKEN`. So, our frontend app needs to read the cookie `XSRF-TOKEN` and set the header `X-XSRF-TOKEN` with the same value while sending any mutating request. This can be easily accomplished with request interceptors. Some frameworks by default do this (`Angular` for example, AFAIK). Look at the following example to send a `POST` request with CSRF Token in header:

```javascript
const addBook = async () => {
  const bookToAdd = {
      title: "Book Title",
      author: "Book Author",
      pages: 300
  };
  const csrfCookie = await cookieStore.get("XSRF-TOKEN");
  const csrfValue = csrfCookie.value;
  const result = await fetch(
      "/resources/books",
      {
          method: "POST",
          headers: {
              "X-XSRF-TOKEN": csrfValue,
              "Content-Type": "application/json",
          },
          body: JSON.stringify(bookToAdd)
      }
  );
  const addedBook = await result.json();
  console.log({ addedBook });
};
```

## Watch Mode

In watch mode, we want to build the application and put the output into the correct location - the root project's `build/resources/main/templates/$appName` directory. If possible, we try to build the app in `development` mode (which is not-optimized for production but is fast, suitable for development). You need to refer to the build-tool which you are using for SPA on how to do this. In any case, you need to create a gradle task for it in each of the SPAs.

### Setup for react-script

`app1/build.gradle.kts`
```kotlin
// ... other configuration
val watchWebapp = tasks.register<NpmTask>("watchWebapp") {
    dependsOn("npmInstall")
    environment.put("BUILD_PATH", "../build/resources/main/templates/app1")
    args.assign(listOf("run", "build:watch"))
}
```

`/app1/package.json`
```json
{
  "watch": {
    "build": {
      "patterns": [
        "."
      ],
      "ignore": "build",
      "extensions": "*",
      "quiet": false
    }
  },
  "scripts": {
    "build": "react-scripts build",
    "build:watch": "npm-watch build"
  },
  "devDependencies": {
    "npm-watch": "0.13.0"
  }
}
```

### Setup for vite

`/app2/build.gradle.kts`
```kotlin
val watchWebapp = tasks.register<NpmTask>("watchWebapp") {
    dependsOn("npmInstall")
    environment.put("NODE_ENV", "development")
    args.assign(listOf("run", "build:watch"))
}
```

`/app2/package.json`
```json
{
   "scripts": {
      "build:watch": "vite build --watch --outDir ../build/resources/main/templates/app2"
   }
}
```

The setup will be similar for other build-tools as well. Read their documentation, if needed.

We need to disable the thymeleaf and spring cache as well:
```properties
# application-local.properties
spring.thymeleaf.cache=false
spring.web.resources.chain.cache=false
```

Now, in order to develop the apps in watch mode, run the Spring Boot Application with `local` profile active (Set `SPRING_PROFILES_ACTIVE=local` environment variable in IDE). Also run the following task for each fo the apps that you want to do develop actively:

```shell
./gradlew :app1:watchWebapp
```

The above task is a blocking task. So run each command in a new tab in terminal.
