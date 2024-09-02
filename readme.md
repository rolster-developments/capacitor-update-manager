# Rolster Capacitor UpdateManager

Plugin for update manager in mobile Android, Huawei and iOS.

## Installation

Package only supports Capacitor 6

```
npm i @rolster/capacitor-update-manager
```

### Android configuration

And register the plugin by adding it to you MainActivity's onCreate:

```java
import com.rolster.capacitor.update.UpdateManagerPlugin;

public class MainActivity extends BridgeActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    registerPlugin(UpdateManagerPlugin.class);
    // Others register plugins

    super.onCreate(savedInstanceState);
  }
}
```
