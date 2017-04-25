# UnifiedPush Server in OpenShift 

Make sure you have at least two 1G persistent volumes provisioned, then create a new project:
```bash
$ oc new-project ups
```

Create the OpenShift UPS application: 
```bash
$ oc new-app -f ups-template.json -n ups
```

Run `oc get pods -w` and monitor until all pods are in the `Running` state, and you're good to go. 
The UnifiedPush server should be available at `http://ups.127.0.0.1.nip.io/ag-push`.

