import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { DataService } from './data.service';
import { environment } from '../environments/environment';
import * as EventSource from 'eventsource';
import { Subscription } from 'rxjs';
import { Http } from '@angular/http';
import 'rxjs/Rx';
import { Observable } from 'rxjs/Observable';
import { RSA_X931_PADDING } from 'constants';
import { ObserveOnOperator } from 'rxjs/operators/observeOn';


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'app';
  subscription1;
  subscription2;
  data;
  data2;
  dataAry = [];
  dataAry$: Observable<any>;
  constructor(private http: Http) {
    
  }

  ngOnInit() {
     /* this.forkJoin(); */
    /* this.flatMap(); */
    /* this.merge(); */
    

   this.dataAry$ = Observable.create((observer) => {
      let eventSource = new EventSource('http://localhost:4040/movies');
      eventSource.onmessage = (event) => {
        console.log('on Message', event);
        this.dataAry.push(JSON.parse(event.data));
        observer.next(this.dataAry);
      };
      eventSource.onerror = (error) => {
        console.log(error);
        // readyState === 0 (closed) means the remote source closed the connection,
        // so we can safely treat it as a normal situation. Another way of detecting the end of the stream
        // is to insert a special element in the stream of events, which the client can identify as the last one.
        if(eventSource.readyState === 0) {
          console.log('The stream has been closed by the server.');
          eventSource.close();
          observer.complete();
        } else {
          observer.error('EventSource error: ' + error);
        }
      }
    });
    
    this.concatMap();
    this.getAllData(); 
    this.takeUntil();
  }

  getAllData() {
    Observable.forkJoin(
      this.http.get('https://myproject-app-7fa35.firebaseio.com/data.json').map(res => res.json()),
      this.http.get('https://myproject-app-7fa35.firebaseio.com/recipes.json').map(res => res.json())
    ).subscribe(
      data => {
        console.log(data[0]);
        console.log(data[1]);
        this.data = data[0];
        this.data2 = data[1];
      },
      error => {
        console.log(error);
      },
      () => {
        console.log('Completed');
      }
    )
  }

  forkJoin() {
    let ob1$ = Observable.of(this.http.get('https://myproject-app-7fa35.firebaseio.com/data.json').map(res => {return res.json();}));
    let ob2$ = Observable.of(this.http.get('https://myproject-app-7fa35.firebaseio.com/recipes.json').map(res => {return res.json();}));
    Observable.forkJoin(ob1$, ob2$)
    .do(console.log).subscribe();
  }

  flatMap() {
    this.http.get('https://myproject-app-7fa35.firebaseio.com/data.json').first()
    .flatMap((data)=> {
      this.data = data.json();
      console.log(data.json());
      return this.http.get('https://myproject-app-7fa35.firebaseio.com/recipes.json');
    }).subscribe(
      (res) => {
        this.data2 = res.json();
        console.log(res.json());
      }
    )
  }

  merge() {
    let ob1$ = this.http.get('https://myproject-app-7fa35.firebaseio.com/data.json');
    let ob2$ = this.http.get('https://myproject-app-7fa35.firebaseio.com/recipes.json');
    ob1$.merge(ob2$).delay(1000).mergeMap(data => {return data.json()}).toArray().subscribe(
      data => {
        console.log('Merge', data);
      }
    );
  }

  concatMap() {
    let ob1$ = this.http.get('https://myproject-app-7fa35.firebaseio.com/data.json');
    let ob2$ = this.http.get('https://myproject-app-7fa35.firebaseio.com/recipes.json');
    ob1$.merge(ob2$).delay(1000).concatMap(data => {return data.json()}).toArray().subscribe(
      data => {
        console.log('Concat Map', data);
      }
    );
  }

  takeUntil() {
    let ob1$ = this.http.get('https://myproject-app-7fa35.firebaseio.com/data.json');
    let ob2$ = this.http.get('https://myproject-app-7fa35.firebaseio.com/recipes.json');
    ob2$.merge(ob1$).map(data => {return data.json()}).skip(1).toArray().subscribe(
      data=> {
        console.log('take', data);
      }
    );
  }

}
